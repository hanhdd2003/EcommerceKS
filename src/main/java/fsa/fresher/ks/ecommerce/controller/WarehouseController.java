package fsa.fresher.ks.ecommerce.controller;

import fsa.fresher.ks.ecommerce.exception.BadRequestException;
import fsa.fresher.ks.ecommerce.exception.ResourceNotFoundException;
import fsa.fresher.ks.ecommerce.model.dto.request.UpdateOrderStatusDTO;
import fsa.fresher.ks.ecommerce.model.dto.response.ListResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.OrderListDTO;
import fsa.fresher.ks.ecommerce.model.entity.Order;
import fsa.fresher.ks.ecommerce.model.entity.OrderItem;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;
import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;
import fsa.fresher.ks.ecommerce.model.enums.PaymentMethod;
import fsa.fresher.ks.ecommerce.repository.InventoryReservationRepository;
import fsa.fresher.ks.ecommerce.repository.OrderRepository;
import fsa.fresher.ks.ecommerce.repository.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final OrderRepository orderRepository;
    private final ProductSkuRepository skuRepository;
    private final InventoryReservationRepository reservationRepository;

    @GetMapping("/orders")
    public ListResponse<OrderListDTO> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Order> ordersPage;

        if (status != null) {
            ordersPage = orderRepository.findByStatus(status, pageable);
        } else {
            ordersPage = orderRepository.findAll(pageable);
        }

        Page<OrderListDTO> dtoPage = ordersPage.map(order ->
                new OrderListDTO(
                        order.getOrderCode(),
                        order.getCustomerName(),
                        order.getCustomerPhone(),
                        order.getShippingAddress(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                )
        );

        return new ListResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }
    @PatchMapping("/orders/{orderCode}/status")
    @Transactional
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable String orderCode,
            @RequestBody UpdateOrderStatusDTO dto
    ) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order không tồn tại"));

        OrderStatus newStatus = dto.getStatus();
        OrderStatus currentStatus = order.getStatus();

        // No-op
        if (currentStatus == newStatus) {
            return ResponseEntity.ok("Trạng thái không thay đổi");
        }

        // Không cho chuyển về PENDING_PAYMENT
        if (newStatus == OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Không thể đổi về trạng thái PENDING_PAYMENT");
        }

        // Validate transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new BadRequestException("Chuyển trạng thái không hợp lệ: " + currentStatus + " -> " + newStatus);
        }

        // Apply side effects on inventory & reservations
        switch (newStatus) {
            case CANCELLED -> {
                if (currentStatus == OrderStatus.SHIPPED) {
                    // Không cho hủy sau khi đã giao (đã chặn bằng transition), giữ nguyên
                } else {
                    // Nếu còn reservation thì trả lại và xóa reservation; nếu không (đã trừ stock trước đó) thì cộng lại stock
                    boolean hadReservations = !reservationRepository.findByOrderId(order.getId()).isEmpty();
                    if (hadReservations) {
                        releaseReservations(order);
                        reservationRepository.deleteByOrderId(order.getId());
                    } else {
                        // Có thể là đơn SEPAY đã trừ stock khi PAID → hoàn trả stock nếu hủy trước khi giao
                        restoreInventory(order);
                    }
                }
            }
            case SHIPPED -> {
                if (order.getPaymentMethod() == PaymentMethod.COD) {
                    // Với COD: chuyển reserved → trừ thật và xóa reservation
                    deductFromReserved(order);
                    reservationRepository.deleteByOrderId(order.getId());
                } else {
                    // SEPAY: đã trừ và xóa reservation ở bước PAID/webhook; cleanup nếu còn sót
                    reservationRepository.deleteByOrderId(order.getId());
                }
            }
            case PAYMENT_FAILED -> {
                // Hoàn trả reservation nếu có và xóa
                releaseReservations(order);
                reservationRepository.deleteByOrderId(order.getId());
            }
            case PROCESSING, SHIPPING, PAID -> {
                // Không thay đổi stock/reserved
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELLED || current == OrderStatus.SHIPPED || current == OrderStatus.PAYMENT_FAILED) {
            return false;
        }

        return switch (current) {
//            case PENDING_PAYMENT -> next == OrderStatus.PAID || next == OrderStatus.CANCELLED || next == OrderStatus.PAYMENT_FAILED;
            case PAID -> next == OrderStatus.PROCESSING;
            case PROCESSING -> next == OrderStatus.SHIPPING || next == OrderStatus.CANCELLED;
            case SHIPPING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            default -> false;
        };
    }


    @Transactional
    public void releaseReservations(Order order) {
        for (OrderItem item : order.getItems()) {
            ProductSku sku = skuRepository.findByIdForUpdate(item.getSku().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));
            int qty = item.getQuantity();
            sku.setReservedQuantity(Math.max(sku.getReservedQuantity() - qty, 0));
            skuRepository.save(sku);
        }
    }

    @Transactional
    public void deductFromReserved(Order order) {
        for (OrderItem item : order.getItems()) {
            ProductSku sku = skuRepository.findByIdForUpdate(item.getSku().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));
            int qty = item.getQuantity();
            sku.setStockQuantity(Math.max(sku.getStockQuantity() - qty, 0));
            sku.setReservedQuantity(Math.max(sku.getReservedQuantity() - qty, 0));
            skuRepository.save(sku);
        }
    }

    @Transactional
    public void restoreInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            ProductSku sku = skuRepository.findByIdForUpdate(item.getSku().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));
            int qty = item.getQuantity();
            sku.setStockQuantity(sku.getStockQuantity() + qty);
            skuRepository.save(sku);
        }
    }


}

