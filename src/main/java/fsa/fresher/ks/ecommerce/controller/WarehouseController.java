package fsa.fresher.ks.ecommerce.controller;

import fsa.fresher.ks.ecommerce.model.dto.request.UpdateOrderStatusDTO;
import fsa.fresher.ks.ecommerce.model.dto.response.ListResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.OrderListDTO;
import fsa.fresher.ks.ecommerce.model.enums.OrderStatus;
import fsa.fresher.ks.ecommerce.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/orders")
    public ListResponse<OrderListDTO> getOrders(@RequestParam(required = false) OrderStatus status,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return warehouseService.getOrders(status, page, size);
    }

    @PatchMapping("/orders/{orderCode}/status")
    public ResponseEntity<String> updateOrderStatus(@PathVariable String orderCode,
                                                    @RequestBody UpdateOrderStatusDTO dto) {
        warehouseService.updateOrder(orderCode, dto);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

}

