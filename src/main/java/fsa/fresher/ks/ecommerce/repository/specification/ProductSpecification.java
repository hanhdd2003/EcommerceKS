package fsa.fresher.ks.ecommerce.repository.specification;

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
            
            // Use a subquery to find products where MIN(price) >= minPrice
            var subquery = query.subquery(Long.class);
            var subRoot = subquery.from(Product.class);
            var subSkuJoin = subRoot.join("skus", JoinType.INNER);
            subquery.select(subRoot.get("id"));
            subquery.groupBy(subRoot.get("id"));
            subquery.having(criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.min(subSkuJoin.get("price")), minPrice));
            
            return root.get("id").in(subquery);
        };
    }

    public static Specification<Product> hasPriceLessThanOrEqualTo(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return null;
            }
            
            var subquery = query.subquery(Long.class);
            var subRoot = subquery.from(Product.class);
            var subSkuJoin = subRoot.join("skus", JoinType.INNER);
            subquery.select(subRoot.get("id"));
            subquery.groupBy(subRoot.get("id"));
            subquery.having(criteriaBuilder.lessThanOrEqualTo(criteriaBuilder.max(subSkuJoin.get("price")), maxPrice));
            
            return root.get("id").in(subquery);
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
