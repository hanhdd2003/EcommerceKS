package fsa.fresher.ks.ecommerce.model.entity;

import fsa.fresher.ks.ecommerce.model.enums.EmailStatus;
import fsa.fresher.ks.ecommerce.model.enums.EmailType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private EmailType emailType;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    private String errorMessage;
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        sentAt = LocalDateTime.now();
    }
}

