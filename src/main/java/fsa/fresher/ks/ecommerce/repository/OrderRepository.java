package fsa.fresher.ks.ecommerce.repository;

import fsa.fresher.ks.ecommerce.model.entity.Order;
import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByTrackingToken(String trackingToken);

    @Query("""
            select o from Order o
            left join fetch o.items oi
            left join fetch oi.sku s
            left join fetch s.product p
            where o.id = :id
            """)
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // Tìm tất cả theo trạng thái với paging
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Tìm tất cả nếu không filter
    Page<Order> findAll(Pageable pageable);
}

