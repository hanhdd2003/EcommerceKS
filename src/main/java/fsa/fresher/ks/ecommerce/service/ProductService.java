package fsa.fresher.ks.ecommerce.service;

import fsa.fresher.ks.ecommerce.model.dto.response.ProductDetailResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.ListResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.ProductItemResponse;

import java.math.BigDecimal;

public interface ProductService {

    ListResponse<ProductItemResponse> getProducts(String categorySlug, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

    ListResponse<ProductItemResponse> getProductsWithSpec(String search, String categorySlug, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

    ProductDetailResponse getProductDetail(Long id);
}
