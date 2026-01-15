package fsa.fresher.ks.ecommerce.repository;

import fsa.fresher.ks.ecommerce.model.dto.response.ProductItemResponse;
import fsa.fresher.ks.ecommerce.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("""
                select new fsa.fresher.ks.ecommerce.model.dto.response.ProductItemResponse(
                    p.id,
                    p.name,
                    p.description,
                    c.name,
                    c.slug,
                    min(s.price),
                    max(s.price),
                    case
                        when sum(s.stockQuantity - s.reservedQuantity) > 0 then true
                        else false
                    end,
                    min(i.url),
                    p.videoUrl
                )
                from Product p
                join p.category c
                join p.skus s
                left join p.images i
                where (:categorySlug is null or c.slug = :categorySlug)
                group by p.id, p.name, p.description, c.name, c.slug, p.videoUrl
                having (:minPrice is null or min(s.price) >= :minPrice)
                  and (:maxPrice is null or max(s.price) <= :maxPrice)
            """)
    Page<ProductItemResponse> findProductList(String categorySlug,
                                              BigDecimal minPrice,
                                              BigDecimal maxPrice,
                                              Pageable pageable);


    @Query("""
                select p from Product p
                join fetch p.category
                where p.id = :id
            """)
    Optional<Product> findProductDetail(Long id);

}
