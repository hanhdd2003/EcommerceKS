package fsa.fresher.ks.ecommerce.repository;

import fsa.fresher.ks.ecommerce.model.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    @Query("select r from InventoryReservation r where r.expiresAt < :now and r.order.status = fsa.fresher.ks.ecommerce.model.enums.OrderStatus.PENDING_PAYMENT")
    List<InventoryReservation> findExpired(@Param("now") LocalDateTime now);

    @Query("select r from InventoryReservation r where r.order.id = :orderId")
    List<InventoryReservation> findByOrderId(@Param("orderId") Long orderId);

    @Modifying
    @Query("delete from InventoryReservation r where r.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}

