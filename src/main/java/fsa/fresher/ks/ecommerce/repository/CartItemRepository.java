package fsa.fresher.ks.ecommerce.repository;

import fsa.fresher.ks.ecommerce.model.entity.Cart;
import fsa.fresher.ks.ecommerce.model.entity.CartItem;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndSku(Cart cart, ProductSku sku);
}

