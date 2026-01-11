package fsa.fresher.ks.ecommerce.model.dto.response;


import java.util.List;

public record ProductDetailResponse(
        Long id,
        String name,
        String description,
        String categoryName,
        String categorySlug,
        String videoUrl,
        List<String> images,
        List<ProductSkuResponse> skus
) {}
