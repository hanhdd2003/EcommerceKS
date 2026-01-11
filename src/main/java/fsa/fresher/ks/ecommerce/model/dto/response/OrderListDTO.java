package fsa.fresher.ks.ecommerce.model.dto.response;

import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderListDTO {
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
