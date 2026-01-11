package fsa.fresher.ks.ecommerce.service;

import fsa.fresher.ks.ecommerce.model.dto.request.CheckoutRequestDTO;
import fsa.fresher.ks.ecommerce.model.dto.response.CheckoutResponseDTO;
import fsa.fresher.ks.ecommerce.model.entity.Order;

public interface CheckoutService {
    CheckoutResponseDTO checkout(CheckoutRequestDTO request);

    void releaseExpiredReservations();

    void markOrderPaid(String orderCode);

    Order getOrderByTrackingToken(String trackingToken);

}
