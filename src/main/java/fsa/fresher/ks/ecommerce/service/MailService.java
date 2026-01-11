package fsa.fresher.ks.ecommerce.service;

import fsa.fresher.ks.ecommerce.model.entity.Order;

public interface MailService {
    void sendOrderConfirmation(Order order);
}