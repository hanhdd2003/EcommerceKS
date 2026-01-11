package fsa.fresher.ks.ecommerce.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO {

    private String cartToken;
    private List<CartItemResponseDTO> items;

    private BigDecimal totalAmount;
}
