package fsa.fresher.ks.ecommerce.controller;

import fsa.fresher.ks.ecommerce.model.dto.request.CheckoutRequestDTO;
import fsa.fresher.ks.ecommerce.model.dto.response.OrderTrackingResponse;
import fsa.fresher.ks.ecommerce.model.entity.Order;
import fsa.fresher.ks.ecommerce.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO request) {
        return ResponseEntity.ok(checkoutService.checkout(request));
    }

    @PostMapping("/payment-success/{orderCode}")
    public ResponseEntity<?> paymentSuccess(@PathVariable String orderCode) {
        checkoutService.markOrderPaid(orderCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tracking/{trackingToken}")
    public ResponseEntity<?> track(@PathVariable String trackingToken) {
        Order order = checkoutService.getOrderByTrackingToken(trackingToken);

        return ResponseEntity.ok(
                new OrderTrackingResponse(
                        order.getOrderCode(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getCreatedAt(),
                        order.getUpdatedAt()
                )
        );
    }

}


