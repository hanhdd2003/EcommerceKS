package fsa.fresher.ks.ecommerce.model.dto.response;

import fsa.fresher.ks.ecommerce.model.enums.Color;
import fsa.fresher.ks.ecommerce.model.enums.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponseDTO {

    private Long skuId;
    private String productName;

    private Size size;
    private Color color;

    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;
}
