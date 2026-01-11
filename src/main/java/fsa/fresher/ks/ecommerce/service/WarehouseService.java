package fsa.fresher.ks.ecommerce.service;

import fsa.fresher.ks.ecommerce.model.dto.request.UpdateOrderStatusDTO;
import fsa.fresher.ks.ecommerce.model.dto.response.ListResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.OrderListDTO;
import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;

public interface WarehouseService {
    ListResponse<OrderListDTO> getOrders(OrderStatus status, int page, int size);

    void updateOrder(String orderCode, UpdateOrderStatusDTO dto);
}
