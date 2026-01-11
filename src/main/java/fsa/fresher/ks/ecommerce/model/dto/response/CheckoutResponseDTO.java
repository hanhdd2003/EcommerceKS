package fsa.fresher.ks.ecommerce.model.dto.response;

import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CheckoutResponseDTO {
    private String orderCode;
    private String trackingToken;
    private OrderStatus status;
    private BigDecimal totalAmount;
}
