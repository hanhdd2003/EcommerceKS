package fsa.fresher.ks.ecommerce.model.dto.response;

import java.math.BigDecimal;

public record ProductItemResponse(
        Long id,
        String name,
        String description,
        String categoryName,
        String categorySlug,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        boolean inStock,
        String firstImageUrl,
        String videoUrl
) {}
