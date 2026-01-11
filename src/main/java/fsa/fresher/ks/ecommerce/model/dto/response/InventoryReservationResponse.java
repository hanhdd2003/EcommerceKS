package fsa.fresher.ks.ecommerce.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InventoryReservationResponse {
    private Long skuId;
    private Integer quantity;
    private LocalDateTime expiresAt;
}