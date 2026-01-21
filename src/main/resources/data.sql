INSERT INTO categories (id, name, slug)
VALUES (1, 'Áo thun', 'ao-thun'),
       (2, 'Hoodie', 'hoodie'),
       (3, 'Quần jean', 'quan-jean');

INSERT INTO products (id, name, description, category_id, created_at, updated_at)
VALUES (1, 'Áo thun nam cổ tròn', 'Áo thun cotton 100%, thấm hút tốt, form regular.', 1, now(), now()),
       (2, 'Hoodie nỉ có mũ', 'Hoodie nỉ ấm, mặt trong lót lông, phù hợp thời tiết se lạnh.', 2, now(), now()),
       (3, 'Quần jean slim fit', 'Quần jean co giãn nhẹ, dáng slim fit, dễ phối đồ.', 3, now(), now()),
       (4, 'Áo thun nữ crop top', 'Áo thun nữ form crop, chất liệu cotton mềm mại, phối được nhiều kiểu.', 1, now(),now()),
       (5, 'Áo thun unisex form rộng', 'Áo thun oversize, phong cách trẻ trung năng động.', 1, now(), now()),
       (6, 'Hoodie zip unisex', 'Hoodie có khóa kéo, túi kangaroo, style basic dễ mặc.', 2, now(), now()),
       (7, 'Quần jean baggy', 'Quần jean ống rộng, phong cách retro.', 3, now(), now()),
       (8, 'Quần jean skinny', 'Quần jean ôm sát, tôn dáng, co giãn tốt.', 3, now(), now());

-- optional demo videos for some products
UPDATE products SET video_url = 'https://cdn.example.com/videos/prod-1.mp4' WHERE id = 1;
UPDATE products SET video_url = 'https://cdn.example.com/videos/prod-2.mp4' WHERE id = 2;

-- product images demo
INSERT INTO product_images (id, product_id, url, sort_order)
VALUES (1, 1, 'https://cdn.example.com/images/at-tron/1.jpg', 1),
       (2, 1, 'https://cdn.example.com/images/at-tron/2.jpg', 2),
       (3, 1, 'https://cdn.example.com/images/at-tron/3.jpg', 3),
       (4, 2, 'https://cdn.example.com/images/hoodie/1.jpg', 1),
       (5, 2, 'https://cdn.example.com/images/hoodie/2.jpg', 2),
       (6, 3, 'https://cdn.example.com/images/jean-slim/1.jpg', 1),
       (7, 4, 'https://cdn.example.com/images/crop-top/1.jpg', 1),
       (8, 5, 'https://cdn.example.com/images/ao-oversize/1.jpg', 1),
       (9, 6, 'https://cdn.example.com/images/hoodie-zip/1.jpg', 1),
       (10, 7, 'https://cdn.example.com/images/jean-baggy/1.jpg', 1),
       (11, 8, 'https://cdn.example.com/images/jean-skinny/1.jpg', 1);

INSERT INTO product_skus (id, sku_code, size, color, price, stock_quantity, reserved_quantity,cart_reserved_quantity, product_id)
VALUES (1, 'AT-TRON-M-DEN', 'M', 'BLACK', 199000.00, 10, 0,0, 1),
       (2, 'AT-TRON-M-TRANG', 'M', 'WHITE', 199000.00, 100, 0,0, 1),
       (3, 'AT-TRON-M-XANH', 'M', 'BLUE', 199000.00, 100, 0,0, 1),
       (4, 'AT-TRON-L-TRANG', 'L', 'WHITE', 199000.00, 80, 0,0, 1),
       (5, 'AT-TRON-XL-XANH', 'XL', 'BLUE', 209000.00, 60, 0,0, 1),
       (6, 'HD-NI-M-DEN', 'M', 'BLACK', 349000.00, 50, 0,0, 2),
       (7, 'HD-NI-L-TRANG', 'L', 'WHITE', 359000.00, 40, 0,0, 2),
       (8, 'HD-NI-L-DEN', 'L', 'BLACK', 359000.00, 40, 0,0, 2),
       (9, 'QJ-SLIM-32-XANH', 'L', 'BLUE', 399000.00, 70, 0, 0,3),
       (10, 'QJ-SLIM-34-DEN', 'XL', 'BLACK', 409000.00, 55, 0,0, 3),
       (11, 'AT-CROP-S-HONG', 'S', 'PINK', 179000.00, 80, 0,0, 4),
       (12, 'AT-CROP-M-HONG', 'M', 'PINK', 179000.00, 70, 0, 0,4),
       (13, 'AT-CROP-S-TIM', 'S', 'PURPLE', 179000.00, 60, 0,0, 4),
       (14, 'AT-OS-M-XAM', 'M', 'GRAY', 189000.00, 90, 0,0, 5),
       (15, 'AT-OS-L-XAM', 'L', 'GRAY', 189000.00, 85, 0,0, 5),
       (16, 'HD-ZIP-M-XAM', 'M', 'GRAY', 379000.00, 45, 0,0, 6),
       (17, 'HD-ZIP-L-DEN', 'L', 'BLACK', 379000.00, 40, 0,0, 6),
       (18, 'QJ-BAG-M-XANH', 'M', 'BLUE', 359000.00, 65, 0,0, 7),
       (19, 'QJ-BAG-L-XANH', 'L', 'BLUE', 359000.00, 60, 0,0, 7),
       (20, 'QJ-SKIN-S-DEN', 'S', 'BLACK', 339000.00, 75, 0,0, 8),
       (21, 'QJ-SKIN-M-DEN', 'M', 'BLACK', 339000.00, 70, 0,0, 8);

