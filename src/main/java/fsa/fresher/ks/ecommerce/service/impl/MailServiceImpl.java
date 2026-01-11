package fsa.fresher.ks.ecommerce.service.impl;

import fsa.fresher.ks.ecommerce.model.entity.Order;
import fsa.fresher.ks.ecommerce.model.entity.OrderItem;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;
import fsa.fresher.ks.ecommerce.repository.OrderRepository;
import fsa.fresher.ks.ecommerce.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final OrderRepository orderRepository;

    @Override
    @Async
    public void sendOrderConfirmation(Order order) {
        // Refetch order with items to avoid LazyInitialization in async thread
        Order fullOrder = orderRepository.findByIdWithItems(order.getId()).orElse(order);

        StringBuilder itemsSection = new StringBuilder();
        itemsSection.append("Danh sách sản phẩm:\n");
        itemsSection.append("--------------------------------------------\n");
        if (fullOrder.getItems() != null && !fullOrder.getItems().isEmpty()) {
            for (OrderItem item : fullOrder.getItems()) {
                ProductSku sku = item.getSku();
                String productName = sku.getProduct() != null ? sku.getProduct().getName() : "Sản phẩm";
                String size = sku.getSize() != null ? sku.getSize().getDisplayName() : "-";
                String color = sku.getColor() != null ? sku.getColor().getDisplayName() : "-";
                String unitPrice = sku.getPrice() != null ? sku.getPrice().toPlainString() : "0";
                String qty = String.valueOf(item.getQuantity());
                String subTotal = (sku.getPrice() != null)
                        ? sku.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())).toPlainString()
                        : "0";

                itemsSection.append("- ")
                        .append(productName)
                        .append(" (Size: ").append(size).append(", Màu: ").append(color).append(") ")
                        .append("| SL: ").append(qty)
                        .append(" | Đơn giá: ").append(unitPrice)
                        .append(" | Tạm tính: ").append(subTotal)
                        .append("\n");
            }
        } else {
            itemsSection.append("(Không có sản phẩm)\n");
        }
        itemsSection.append("--------------------------------------------\n\n");

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(fullOrder.getCustomerEmail());
        mail.setSubject("Xác nhận đơn hàng " + fullOrder.getOrderCode());
        mail.setText("""
                Cảm ơn bạn đã mua hàng!

                Mã đơn: %s
                Trạng thái: %s
                Tổng tiền: %s

                %s
                Theo dõi đơn hàng tại:
                http://localhost:8080/api/checkout/tracking/%s
                """.formatted(
                fullOrder.getOrderCode(),
                fullOrder.getStatus(),
                fullOrder.getTotalAmount(),
                itemsSection.toString(),
                fullOrder.getTrackingToken()
        ));
        mailSender.send(mail);
    }
}
