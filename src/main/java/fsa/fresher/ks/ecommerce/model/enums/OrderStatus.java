package fsa.fresher.ks.ecommerce.model.enums;

public enum OrderStatus {
    //COD
    PROCESSING, // chờ shop xác nhận
    // SEPAY
    PENDING_PAYMENT, // đang chờ thanh toán
    PAID, // đã thanh toán
    PAYMENT_FAILED, // thanh toán lỗi
    // admin đổi trạng thái đơn hàng
    SHIPPING,  // đang ship
    SHIPPED,  // ship và thanh toán thành công
    CANCELLED // hủy đơn
}
