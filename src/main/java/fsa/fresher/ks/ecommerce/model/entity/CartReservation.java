package fsa.fresher.ks.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "sku_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cart cart;

    @ManyToOne(optional = false)
    private ProductSku sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
