package fsa.fresher.ks.ecommerce.model.dto.response;

import java.math.BigDecimal;

public record ProductSkuResponse(
        Long id,
        String skuCode,
        String size,
        String color,
        BigDecimal price,
        Integer availableStock
) {}