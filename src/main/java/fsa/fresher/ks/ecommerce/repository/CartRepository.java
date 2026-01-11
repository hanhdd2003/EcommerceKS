package fsa.fresher.ks.ecommerce.repository;

import fsa.fresher.ks.ecommerce.model.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCartToken(String cartToken);
}
