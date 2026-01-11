package fsa.fresher.ks.ecommerce.model.dto.request;

import fsa.fresher.ks.ecommerce.model.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequestDTO {
    private String cartToken;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private PaymentMethod paymentMethod; // COD | SEPAY
}

