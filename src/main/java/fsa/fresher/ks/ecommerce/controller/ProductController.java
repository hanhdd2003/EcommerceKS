package fsa.fresher.ks.ecommerce.controller;

import fsa.fresher.ks.ecommerce.model.dto.response.ProductDetailResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.ListResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.ProductItemResponse;
import fsa.fresher.ks.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/simp")
    public ListResponse<ProductItemResponse> getProducts(@RequestParam(value = "category", required = false) String category,
                                                         @RequestParam(required = false) BigDecimal minPrice,
                                                         @RequestParam(required = false) BigDecimal maxPrice,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return productService.getProducts(category, minPrice, maxPrice, page, size);
    }

    @GetMapping()
    public ListResponse<ProductItemResponse> getProductsWithSpec(@RequestParam(required = false) String search,
                                                                 @RequestParam(value = "category", required = false) String category,
                                                                 @RequestParam(required = false) BigDecimal minPrice,
                                                                 @RequestParam(required = false) BigDecimal maxPrice,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return productService.getProductsWithSpec(search, category, minPrice, maxPrice, page, size);
    }


    @GetMapping("/{id}")
    public ProductDetailResponse getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }
}
