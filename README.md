### Ecommerce – Backend (Spring Boot)

Ứng dụng backend thương mại điện tử (demo) xây dựng bằng Spring Boot 4, JPA, MySQL. Hỗ trợ luồng giỏ hàng, đặt hàng (COD/SEPAY), giữ hàng tạm thời (inventory reservation), webhook thanh toán, theo dõi đơn, và API quản trị kho (warehouse) để xử lý trạng thái đơn.

---

### Tính năng chính

- Sản phẩm: Danh sách, lọc theo danh mục/giá, xem chi tiết + biến thể (SKU: kích cỡ/màu sắc).
- Giỏ hàng ẩn danh bằng Cart-Token (header).
- Đặt hàng: COD hoặc SEPAY.
- Giữ hàng tạm thời (reserved) khi tạo order, tự động hết hạn sau 15 phút với SEPAY nếu chưa thanh toán.
- Webhook thanh toán SEPAY: xác nhận thanh toán, trừ kho, gửi email xác nhận.
- Theo dõi đơn bằng tracking token.
- Kho/Quản trị: Danh sách đơn và cập nhật trạng thái theo luồng hợp lệ, ràng buộc qua header `X-WAREHOUSE-KEY`.
- Xử lý tồn kho: stockQuantity, reservedQuantity, kiểm tra đủ hàng, khôi phục khi hủy/thất bại.
- Chuẩn lỗi JSON thống nhất (status, message, timestamp).

---

### Kiến trúc & công nghệ

- Java 17, Spring Boot 4 (Web, Data JPA, Validation, Mail), Lombok
- MySQL (JPA/Hibernate)
- Lập lịch (Scheduled) giải phóng reservation hết hạn mỗi 60 giây
- Mail SMTP (Gmail) gửi xác nhận đơn hàng
- Interceptor xác thực cho API kho

Các gói quan trọng:
- controller: API layer (Cart, Product, Checkout, Payment, Warehouse)
- service + service.impl: xử lý nghiệp vụ (giỏ hàng, checkout, kho, mail)
- model: entity, enum, DTO request/response
- repository: JPA repositories
- config: Interceptor, WebConfig

---

### Yêu cầu hệ thống

- JDK 17
- Maven 3.9+
- MySQL 8.x (hoặc tương thích)
- (Tùy chọn) IntelliJ IDEA

---

### Cài đặt & cấu hình

1) Clone dự án và mở bằng IntelliJ hoặc IDE bất kỳ.

2) Tạo database MySQL:
```
CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3) Cấu hình `src/main/resources/application.properties`:
- URL DB: `spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce?...`
- User/Pass DB: `spring.datasource.username`, `spring.datasource.password`
- JPA: `spring.jpa.hibernate.ddl-auto=create-drop` (dev); đổi sang `update`/`none` cho môi trường khác.
- Mail (Gmail SMTP):
  - `spring.mail.username`: địa chỉ Gmail
  - `spring.mail.password`: App Password (không dùng mật khẩu thường). Vào Google Account -> Security -> App passwords.
- Warehouse API Key: `warehouse.api.key=YOUR_SECRET_WAREHOUSE_KEY`

Lưu ý bảo mật: Không commit thông tin nhạy cảm (mật khẩu DB, app password Gmail, keys) trong sản phẩm thực tế.

4) Dữ liệu mẫu: `src/main/resources/data.sql` sẽ chạy khi khởi động (do `ddl-auto=create-drop`).

---

### Chạy ứng dụng

- Dùng Maven:
```
mvn spring-boot:run
```

Hoặc build jar:
```
mvn clean package
java -jar target/Ecommerce-0.0.1-SNAPSHOT.jar
```

Mặc định ứng dụng chạy tại: `http://localhost:8080`

Timezone: Asia/Ho_Chi_Minh (cấu hình Jackson/Hibernate).

---

### Quy ước đối tượng & trạng thái đơn

- Trạng thái (`OrderStatus`):
  - `PENDING_PAYMENT` (SEPAY chờ thanh toán)
  - `PAID` (đã thanh toán thành công; chờ xử lý)
  - `PROCESSING` (xử lý đơn)
  - `SHIPPING` (đang giao)
  - `SHIPPED` (đã giao)
  - `CANCELLED` (đã hủy)
  - `PAYMENT_FAILED` (thanh toán thất bại hoặc hết hàng sau thanh toán)

- Phương thức thanh toán (`PaymentMethod`): `COD`, `SEPAY`

- Tồn kho SKU:
  - `stockQuantity`: tồn thực tế
  - `reservedQuantity`: số lượng đang giữ tạm cho các order chưa hoàn tất
  - `availableStock = stockQuantity - reservedQuantity`

---

### Luồng nghiệp vụ chính

1) Cart
- Khách hàng tương tác bằng header `Cart-Token` (một giá trị tùy ý phía client tự generic nếu chưa có, giữ cố định).
- Thêm/Xóa/Đổi số lượng dựa trên `skuId`.

2) Checkout
- COD: giữ hàng (reservation không hết hạn), set trạng thái `PROCESSING`, gửi email xác nhận.
- SEPAY: giữ hàng với thời hạn 15 phút, set `PENDING_PAYMENT`. Nếu quá hạn, job sẽ hủy giữ hàng và set đơn `CANCELLED`.

3) Thanh toán SEPAY (Webhook)
- Khi webhook báo `SUCCESS`:
  - Kiểm tra lại tồn; nếu thiếu -> set `PAYMENT_FAILED` và giải phóng reservation.
  - Nếu đủ -> trừ stock thực, giảm reserved tương ứng, xóa reservation, set `PAID`, gửi email xác nhận.
- Khi webhook báo thất bại -> set `PAYMENT_FAILED`, giải phóng reservation.

4) Kho/Quản trị
- Xem danh sách đơn theo trang và lọc theo trạng thái.
- Cập nhật trạng thái theo luồng hợp lệ (ví dụ: `PAID -> PROCESSING -> SHIPPING -> SHIPPED`, hủy hợp lệ ở một số bước).
- Với COD, khi chuyển `SHIPPED`, hệ thống trừ stock thực và xóa reservation.

---

### Chuẩn lỗi API

Tất cả lỗi trả về dạng JSON:
```
{
  "status": 400,
  "message": "Chuỗi mô tả lỗi",
  "timestamp": "2026-01-11T19:46:00"
}
```

---

### Tài liệu API

Base URL: `http://localhost:8080`

1) Sản phẩm
- GET `/api/products`
  - Query: `category` (slug, tùy chọn), `minPrice`, `maxPrice`, `page` (mặc định 0), `size` (mặc định 10)
  - Response: `ListResponse<ProductItemResponse>`
  - Ví dụ:
```
curl "http://localhost:8080/api/products?category=shoes&minPrice=100000&maxPrice=1500000&page=0&size=12"
```

- GET `/api/products/{id}`
  - Response: `ProductDetailResponse`
```
curl http://localhost:8080/api/products/1
```

2) Giỏ hàng (cần header `Cart-Token`)
- GET `/api/cart`
```
curl -H "Cart-Token: 123e4567" http://localhost:8080/api/cart
```

- POST `/api/cart` (thêm vào giỏ)
  - Params: `skuId`, `quantity`
```
curl -X POST "http://localhost:8080/api/cart?skuId=10&quantity=2" \
     -H "Cart-Token: 123e4567"
```

- PUT `/api/cart` (cập nhật số lượng)
```
curl -X PUT "http://localhost:8080/api/cart?skuId=10&quantity=5" \
     -H "Cart-Token: 123e4567"
```

- DELETE `/api/cart` (xóa item)
```
curl -X DELETE "http://localhost:8080/api/cart?skuId=10" \
     -H "Cart-Token: 123e4567"
```

3) Checkout
- POST `/api/checkout`
  - Body JSON (`CheckoutRequestDTO`):
```
{
  "cartToken": "123e4567",
  "customerName": "Nguyen Van A",
  "customerPhone": "0900000000",
  "customerEmail": "a@example.com",
  "shippingAddress": "123 Le Loi, Q.1, HCM",
  "paymentMethod": "COD" // hoặc "SEPAY"
}
```
  - Ví dụ cURL:
```
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{
        "cartToken":"123e4567",
        "customerName":"Nguyen Van A",
        "customerPhone":"0900000000",
        "customerEmail":"a@example.com",
        "shippingAddress":"123 Le Loi, Q.1, HCM",
        "paymentMethod":"SEPAY"
      }'
```

- POST `/api/checkout/payment-success/{orderCode}`
  - Dùng trong demo để set đơn `PAID` thủ công nếu cần.
```
curl -X POST http://localhost:8080/api/checkout/payment-success/ORDERCODE123
```

- GET `/api/checkout/tracking/{trackingToken}`
```
curl http://localhost:8080/api/checkout/tracking/abcd-efgh-1234
```

4) Payment – SEPAY Webhook
- POST `/api/payment/sepay/webhook`
  - Body JSON (`SepayWebhookDTO`):
```
{
  "trackingToken": "abcd-efgh-1234",
  "status": "SUCCESS" // hoặc "FAILED"
}
```
  - Hệ thống sẽ kiểm tra tồn kho, cập nhật trạng thái (`PAID` / `PAYMENT_FAILED`), trừ/giải phóng kho, xóa reservation, và gửi email khi thành công.

5) Warehouse (cần header `X-WAREHOUSE-KEY`)
- GET `/api/warehouse/orders?status=PAID&page=0&size=20`
```
curl "http://localhost:8080/api/warehouse/orders?status=PROCESSING&page=0&size=20" \
     -H "X-WAREHOUSE-KEY: YOUR_SECRET_WAREHOUSE_KEY"
```

- PATCH `/api/warehouse/orders/{orderCode}/status`
  - Body JSON (`UpdateOrderStatusDTO`):
```
{
  "status": "PROCESSING" // hoặc "SHIPPING", "SHIPPED", "CANCELLED", "PAYMENT_FAILED"
}
```
  - Ví dụ cURL:
```
curl -X PATCH http://localhost:8080/api/warehouse/orders/ORDERCODE123/status \
     -H "Content-Type: application/json" \
     -H "X-WAREHOUSE-KEY: YOUR_SECRET_WAREHOUSE_KEY" \
     -d '{"status":"SHIPPING"}'
```

Luồng chuyển trạng thái hợp lệ (tóm tắt):
- `PAID -> PROCESSING`
- `PROCESSING -> SHIPPING` hoặc `CANCELLED`
- `SHIPPING -> SHIPPED` hoặc `CANCELLED`
- Không cho chuyển về `PENDING_PAYMENT`. `CANCELLED/SHIPPED/PAYMENT_FAILED` là trạng thái kết thúc.

Tác động kho khi đổi trạng thái:
- `CANCELLED`: nếu còn reservation -> giải phóng; nếu trước đó đã trừ stock (SEPAY sau webhook) -> cộng lại stock.
- `SHIPPED`:
  - COD: trừ stock thực, xóa reservation.
  - SEPAY: stock đã trừ ở webhook; chỉ dọn reservation còn sót.
- `PAYMENT_FAILED`: giải phóng reservation.

---

### DTO chính

- `CheckoutRequestDTO`: `cartToken`, `customerName`, `customerPhone`, `customerEmail`, `shippingAddress`, `paymentMethod`.
- `CheckoutResponseDTO`: `orderCode`, `trackingToken`, `status`, `totalAmount`.
- `ProductItemResponse`, `ProductDetailResponse`, `ProductSkuResponse`.
- `ListResponse<T>`: phân trang chuẩn: `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`.
- `OrderListDTO`: thông tin tóm tắt order cho kho.
- `UpdateOrderStatusDTO`: `status`.
- `SepayWebhookDTO`: `trackingToken`, `status`.

---

### Mẹo & lưu ý khi chạy demo

- Đặt `Cart-Token` bất kỳ (ví dụ UUID) và giữ cố định phía client đến khi checkout.
- Với SEPAY: sau khi checkout, nếu không gọi webhook SUCCESS trong 15 phút, reservation sẽ hết hạn và đơn có thể bị `CANCELLED` bởi job.
- Demo webhook: gọi thủ công endpoint webhook như trong ví dụ (do chưa tích hợp SEPAY thực).
- Email: cần bật App Password cho Gmail; hoặc dùng provider SMTP khác và chỉnh lại cấu hình.

---

### Phát triển & mở rộng

- Đổi `ddl-auto` sang `update` hoặc dùng Flyway/Liquibase để quản lý schema.
- Bổ sung xác thực người dùng (JWT) và phân quyền.
- Hoàn thiện tích hợp cổng thanh toán thực tế (redirect/return URL, checksum).
- Ghi log, metrics, tracing; thêm OpenAPI/Swagger cho tài liệu API tự động.
