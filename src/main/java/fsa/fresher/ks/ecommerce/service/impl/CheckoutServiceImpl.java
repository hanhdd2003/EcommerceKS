package fsa.fresher.ks.ecommerce.service.impl;

import fsa.fresher.ks.ecommerce.exception.BadRequestException;
import fsa.fresher.ks.ecommerce.exception.ResourceNotFoundException;
import fsa.fresher.ks.ecommerce.model.dto.request.CheckoutRequestDTO;
import fsa.fresher.ks.ecommerce.model.dto.response.CheckoutResponseDTO;
import fsa.fresher.ks.ecommerce.model.entity.*;
import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;
import fsa.fresher.ks.ecommerce.model.enums.PaymentMethod;
import fsa.fresher.ks.ecommerce.repository.*;
import fsa.fresher.ks.ecommerce.service.CheckoutService;
import fsa.fresher.ks.ecommerce.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final ProductSkuRepository skuRepository;
    private final OrderRepository orderRepository;
    private final InventoryReservationRepository reservationRepository;
    private final MailService mailService;
    private final CartReservationRepository cartReservationRepository;


    @Override
    @Transactional
    public CheckoutResponseDTO checkout(CheckoutRequestDTO request) {
        Cart cart = cartRepository.findByCartToken(request.getCartToken())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        Order order = new Order();
        order.setOrderCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        order.setTrackingToken(UUID.randomUUID().toString().replace("-", ""));
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setSku(ci.getSku());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getSku().getPrice());
            orderItems.add(oi);

            total = total.add(ci.getSku().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setItems(orderItems);
        order.setTotalAmount(total);

        // **Lưu order trước để có ID**
        orderRepository.save(order);

        if (request.getPaymentMethod() == PaymentMethod.COD) {
            reserveForCheckout(order, cart, cart.getItems(), null);
            order.setStatus(OrderStatus.PROCESSING);
            mailService.sendOrderConfirmation(order);
        } else {
            LocalDateTime expireAt = LocalDateTime.now().plusMinutes(15);
            reserveForCheckout(order, cart, cart.getItems(), expireAt);
            order.setStatus(OrderStatus.PENDING_PAYMENT);
        }

        // Lưu lại status cuối cùng
        orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return new CheckoutResponseDTO(order.getOrderCode(), order.getTrackingToken(), order.getStatus(), total);
    }


    @Transactional
    protected void reserveForCheckout(
            Order order,
            Cart cart,
            List<CartItem> items,
            LocalDateTime expireAt
    ) {
        for (CartItem item : items) {

            ProductSku sku = skuRepository
                    .findByIdForUpdate(item.getSku().getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("SKU không tồn tại"));

            int quantity = item.getQuantity();

            CartReservation cartReservation =
                    cartReservationRepository.findByCartAndSku(cart, sku).orElse(null);

            if (cartReservation != null) {
                // ===== CASE 1: CÒN GIỮ CART =====
                if (cartReservation.getQuantity() < quantity) {
                    throw new BadRequestException("Số lượng giữ không đủ");
                }

                sku.setCartReservedQuantity(
                        sku.getCartReservedQuantity() - quantity
                );
                sku.setReservedQuantity(
                        sku.getReservedQuantity() + quantity
                );

                cartReservationRepository.delete(cartReservation);

            } else {
                // ===== CASE 2: HẾT GIỮ, GIỮ TỪ KHO =====
                if (sku.getAvailableStock() < quantity) {
                    throw new BadRequestException("Sản phẩm đã hết hàng");
                }

                sku.setReservedQuantity(
                        sku.getReservedQuantity() + quantity
                );
            }

            skuRepository.save(sku);

            // ===== TẠO INVENTORY RESERVATION =====
            InventoryReservation ir = new InventoryReservation();
            ir.setOrder(order);
            ir.setSku(sku);
            ir.setQuantity(quantity);
            ir.setExpiresAt(
                    expireAt != null
                            ? expireAt
                            : LocalDateTime.of(9999, 12, 31, 23, 59, 59)
            );

            reservationRepository.save(ir);
        }
    }



    @Override
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseExpiredReservations() {
        List<InventoryReservation> expired = reservationRepository.findExpired(LocalDateTime.now());
        for (InventoryReservation r : expired) {
            ProductSku sku = skuRepository.findByIdForUpdate(r.getSku().getId())
                    .orElseThrow();
            sku.setReservedQuantity(sku.getReservedQuantity() - r.getQuantity());
            skuRepository.save(sku);

            Order order = r.getOrder();
            if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }

            reservationRepository.delete(r);
        }
    }

    @Override
    @Transactional
    public void markOrderPaid(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) return;

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByTrackingToken(String trackingToken) {
        return orderRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    }

}
