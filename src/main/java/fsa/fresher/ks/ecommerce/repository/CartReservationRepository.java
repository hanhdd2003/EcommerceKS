package fsa.fresher.ks.ecommerce.repository;

import fsa.fresher.ks.ecommerce.model.entity.Cart;
import fsa.fresher.ks.ecommerce.model.entity.CartReservation;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartReservationRepository
        extends JpaRepository<CartReservation, Long> {

    Optional<CartReservation> findByCartAndSku(Cart cart, ProductSku sku);

    @Query("""
                SELECT r FROM CartReservation r
                WHERE r.expiresAt < :now
            """)
    List<CartReservation> findExpired(LocalDateTime now);
}

