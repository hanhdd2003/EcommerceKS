package fsa.fresher.ks.ecommerce.model.dto.request;

import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusDTO {
    private OrderStatus status; // CONFIRMED, SHIPPING, SHIPPED, DELIVERY_FAILED, CANCELLED
}
