package fsa.fresher.ks.ecommerce.specification;

import fsa.fresher.ks.ecommerce.model.entity.Category;
import fsa.fresher.ks.ecommerce.model.entity.Product;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasCategorySlug(String categorySlug) {
        return (root, query, criteriaBuilder) -> {
            if (categorySlug == null || categorySlug.isEmpty()) {
                return null;
            }
            Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
            return criteriaBuilder.equal(categoryJoin.get("slug"), categorySlug);
        };
    }

    public static Specification<Product> hasPriceGreaterThanOrEqualTo(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return null;
            }
            Join<Product, ProductSku> skuJoin = root.join("skus", JoinType.INNER);
            query.distinct(true);
            return criteriaBuilder.greaterThanOrEqualTo(skuJoin.get("price"), minPrice);
        };
    }

    public static Specification<Product> hasPriceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return null;
            }
            Join<Product, ProductSku> skuJoin = root.join("skus", JoinType.INNER);
            query.distinct(true);
            return criteriaBuilder.lessThanOrEqualTo(skuJoin.get("price"), maxPrice);
        };
    }

    public static Specification<Product> hasSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) {
                return null;
            }
            String searchPattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern);
        };
    }
}
