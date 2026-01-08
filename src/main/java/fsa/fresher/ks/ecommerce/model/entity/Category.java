package fsa.fresher.ks.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // áo thun, hoodie

    private String slug; // ao-thun, hoodie

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
