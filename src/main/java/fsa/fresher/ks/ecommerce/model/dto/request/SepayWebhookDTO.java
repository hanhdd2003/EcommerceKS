package fsa.fresher.ks.ecommerce.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SepayWebhookDTO {
    private String trackingToken;
    private String status; // "SUCCESS" hoặc "FAILED"
    private BigDecimal amount;
    private String signature; // nếu SePay gửi signature để xác thực
}
