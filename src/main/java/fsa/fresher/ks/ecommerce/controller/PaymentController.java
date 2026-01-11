package fsa.fresher.ks.ecommerce.controller;

import fsa.fresher.ks.ecommerce.exception.ResourceNotFoundException;
import fsa.fresher.ks.ecommerce.model.dto.request.SepayWebhookDTO;
import fsa.fresher.ks.ecommerce.model.entity.Order;
import fsa.fresher.ks.ecommerce.model.entity.OrderItem;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;
import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;
import fsa.fresher.ks.ecommerce.repository.InventoryReservationRepository;
import fsa.fresher.ks.ecommerce.repository.OrderRepository;
import fsa.fresher.ks.ecommerce.repository.ProductSkuRepository;
import fsa.fresher.ks.ecommerce.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;
    private final ProductSkuRepository skuRepository;
    private final MailService mailService;
    private final InventoryReservationRepository reservationRepository;

    @PostMapping("/sepay/webhook")
    @Transactional
    public ResponseEntity<String> sepayWebhook(@RequestBody SepayWebhookDTO webhook) {

        Order order = orderRepository.findByTrackingToken(webhook.getTrackingToken())
                .orElseThrow(() -> new ResourceNotFoundException("Order không tồn tại"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return ResponseEntity.ok("Order đã được xử lý trước đó");
        }

        if ("SUCCESS".equalsIgnoreCase(webhook.getStatus())) {
            // Thanh toán thành công
            // Kiểm tra stock trước khi confirm PAID
            boolean stockAvailable = true;
            for (OrderItem item : order.getItems()) {
                ProductSku sku = skuRepository.findByIdForUpdate(item.getSku().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));
                if (sku.getAvailableStock() < item.getQuantity()) {
                    stockAvailable = false;
                    break;
                }
            }

            if (!stockAvailable) {
                // Nếu không đủ hàng, giữ order ở trạng thái PENDING_PAYMENT hoặc INVENTORY_FAILED
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                orderRepository.save(order);
                return ResponseEntity.ok("Thanh toán thành công nhưng hàng không đủ trong kho. Admin cần xử lý.");
            }

            // Giảm stock và release reservation
            for (OrderItem item : order.getItems()) {
                ProductSku sku = skuRepository.findByIdForUpdate(item.getSku().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));
                int qty = item.getQuantity();
                sku.setStockQuantity(sku.getStockQuantity() - qty);
                sku.setReservedQuantity(Math.max(sku.getReservedQuantity() - qty, 0));
                skuRepository.save(sku);
            }

            // Xóa toàn bộ reservation theo order
            reservationRepository.deleteByOrderId(order.getId());

            // Cập nhật trạng thái order
            order.setStatus(OrderStatus.PAID); // Thanh toán thành công, chờ admin xử lý PROCESSING
            orderRepository.save(order);

            // Gửi email xác nhận thanh toán
            mailService.sendOrderConfirmation(order);

        } else {
            // Thanh toán thất bại
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);

            // Trả lại stock reservation
            for (OrderItem item : order.getItems()) {
                ProductSku sku = skuRepository.findByIdForUpdate(item.getSku().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));
                int qty = item.getQuantity();
                sku.setReservedQuantity(Math.max(sku.getReservedQuantity() - qty, 0));
                skuRepository.save(sku);
            }

            // Xóa reservation
            reservationRepository.deleteByOrderId(order.getId());
        }

        return ResponseEntity.ok("Webhook xử lý thành công");
    }


}

