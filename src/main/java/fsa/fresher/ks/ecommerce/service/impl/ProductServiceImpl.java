package fsa.fresher.ks.ecommerce.service.impl;

import fsa.fresher.ks.ecommerce.exception.ResourceNotFoundException;
import fsa.fresher.ks.ecommerce.model.dto.MappingDto;
import fsa.fresher.ks.ecommerce.model.dto.response.ProductDetailResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.ProductItemResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.ListResponse;
import fsa.fresher.ks.ecommerce.model.entity.Product;
import fsa.fresher.ks.ecommerce.repository.ProductRepository;
import fsa.fresher.ks.ecommerce.service.ProductService;
import fsa.fresher.ks.ecommerce.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ListResponse<ProductItemResponse> getProducts(String categorySlug, BigDecimal minPrice,
                                                         BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductItemResponse> pageData = productRepository.findProductList(categorySlug, minPrice, maxPrice, pageable);
        return new ListResponse<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }

    @Override
    public ListResponse<ProductItemResponse> getProductsWithSpec(String search, String categorySlug, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Product> spec = (root, query, criteriaBuilder) -> null;

        if (search != null && !search.isEmpty()) {
            spec = spec.and(ProductSpecification.hasSearch(search));
        }

        if (categorySlug != null && !categorySlug.isEmpty()) {
            spec = spec.and(ProductSpecification.hasCategorySlug(categorySlug));
        }

        if (minPrice != null) {
            spec = spec.and(ProductSpecification.hasPriceGreaterThanOrEqualTo(minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and(ProductSpecification.hasPriceLessThanOrEqualTo(maxPrice));
        }

        Page<Product> pageData = productRepository.findAll(spec, pageable);

        List<ProductItemResponse> content = pageData.getContent().stream()
                .map(MappingDto::productToItemResponse)
                .toList();

        return new ListResponse<>(
                content,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }

    public ProductDetailResponse getProductDetail(Long id) {
        Product product = productRepository.findProductDetail(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        return MappingDto.productToDetailResponse(product);
    }

 /*
    // Chuyển tên hoặc slug bất kỳ thành slug chuẩn (lowercase, bỏ dấu, thay khoảng trắng bằng '-')
        private String toSlug(String input) {
        if (input == null) return null;
        String lower = input.toLowerCase();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        String noDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // thay mọi ký tự không phải chữ/số bằng dấu gạch ngang
        String slug = noDiacritics.replaceAll("[^a-z0-9]+", "-");
        // bỏ gạch ngang đầu/cuối và chuẩn hóa nhiều gạch liên tiếp
        slug = slug.replaceAll("-+", "-");
        slug = slug.replaceAll("(^-|-$)", "");
        return slug;
    }
*/

}

