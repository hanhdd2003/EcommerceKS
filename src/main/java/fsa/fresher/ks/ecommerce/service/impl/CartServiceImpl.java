package fsa.fresher.ks.ecommerce.service.impl;

import fsa.fresher.ks.ecommerce.exception.BadRequestException;
import fsa.fresher.ks.ecommerce.exception.ResourceNotFoundException;
import fsa.fresher.ks.ecommerce.model.dto.MappingDto;
import fsa.fresher.ks.ecommerce.model.dto.response.CartResponseDTO;
import fsa.fresher.ks.ecommerce.model.entity.Cart;
import fsa.fresher.ks.ecommerce.model.entity.CartItem;
import fsa.fresher.ks.ecommerce.model.entity.CartReservation;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;
import fsa.fresher.ks.ecommerce.repository.CartItemRepository;
import fsa.fresher.ks.ecommerce.repository.CartRepository;
import fsa.fresher.ks.ecommerce.repository.CartReservationRepository;
import fsa.fresher.ks.ecommerce.repository.ProductSkuRepository;
import fsa.fresher.ks.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductSkuRepository skuRepository;
    private final CartReservationRepository cartReservationRepository;

    public Cart getOrCreateCart(String cartToken) {
        return cartRepository.findByCartToken(cartToken)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setCartToken(cartToken);
                    cart.setItems(new ArrayList<>());
                    return cartRepository.save(cart);
                });
    }

    /* Thêm sản phẩm vào giỏ */
    @Override
    @Transactional
    public CartResponseDTO addToCart(String cartToken, Long skuId, int quantity) {

        if (quantity == 0) {
            throw new BadRequestException("Số lượng không hợp lệ");
        }

        ProductSku sku = skuRepository.findByIdForUpdate(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));

        Cart cart = getOrCreateCart(cartToken);

        CartItem cartItem = cartItemRepository.findByCartAndSku(cart, sku).orElse(null);

        int currentQty = cartItem != null ? cartItem.getQuantity() : 0;
        int newQty = currentQty + quantity;

        if (newQty <= 0) {
            return removeItem(cartToken, skuId);
        }

        int delta = newQty - currentQty;

        if (delta > 0 && sku.getAvailableStock() < delta) {
            throw new BadRequestException("Số lượng vượt quá tồn kho");
        }

        // --- CART ITEM ---
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setSku(sku);
            cartItem.setQuantity(newQty);
            cart.getItems().add(cartItem);
        } else {
            cartItem.setQuantity(newQty);
        }

        // --- CART RESERVATION ---
        CartReservation cr = cartReservationRepository
                .findByCartAndSku(cart, sku)
                .orElseGet(() -> {
                    CartReservation r = new CartReservation();
                    r.setCart(cart);
                    r.setSku(sku);
                    r.setQuantity(0);
                    return r;
                });

        cr.setQuantity(cr.getQuantity() + delta);
        cr.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        sku.setCartReservedQuantity(
                sku.getCartReservedQuantity() + delta
        );

        cartReservationRepository.save(cr);
        skuRepository.save(sku);

        return MappingDto.cartToResponseDTO(cartRepository.save(cart));
    }

    /* Cập nhật số lượng (tăng / giảm), thay thế quantity vào item đã tồn tại, không cộng như ở phần add */
    @Override
    @Transactional
    public CartResponseDTO updateQuantity(String cartToken, Long skuId, int quantity) {
        if (quantity <= 0) {
            return removeItem(cartToken, skuId);
        }

        // 1. Lock SKU
        ProductSku sku = skuRepository.findByIdForUpdate(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));

        // 2. Lấy cart
        Cart cart = getOrCreateCart(cartToken);

        // 3. Lấy CartItem
        CartItem cartItem = cartItemRepository.findByCartAndSku(cart, sku)
                .orElseThrow(() -> new ResourceNotFoundException("Item không tồn tại trong giỏ"));

        int oldQty = cartItem.getQuantity();
        int newQty = quantity;
        int delta = newQty - oldQty;

        // 4. Nếu tăng → check tồn kho
        if (delta > 0 && sku.getAvailableStock() < delta) {
            throw new BadRequestException("Số lượng vượt quá tồn kho");
        }

        // 5. Update cart item
        cartItem.setQuantity(newQty);

        // 6. Lấy CartReservation
        CartReservation cartReservation = cartReservationRepository
                .findByCartAndSku(cart, sku)
                .orElseThrow(() ->
                        new BadRequestException("Hết thời gian giữ hàng, vui lòng thêm lại"));

        // 7. Update reservation
        if (cartReservation.getQuantity() + delta < 0) {
            throw new IllegalStateException("CartReservation quantity < 0");
        }
        cartReservation.setQuantity(cartReservation.getQuantity() + delta);
        cartReservation.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        // 8. Update SKU
        sku.setCartReservedQuantity(
                sku.getCartReservedQuantity() + delta
        );

        // Safety
        if (sku.getCartReservedQuantity() < 0) {
            sku.setCartReservedQuantity(0);
        }

        // 9. Save
        skuRepository.save(sku);
        cartReservationRepository.save(cartReservation);
        cartRepository.save(cart);

        return MappingDto.cartToResponseDTO(cart);
    }


    /* Xóa item khỏi giỏ */
    @Override
    @Transactional
    public CartResponseDTO removeItem(String cartToken, Long skuId) {

        // 1. Lấy cart
        Cart cart = getOrCreateCart(cartToken);

        // 2. Lock SKU
        ProductSku sku = skuRepository.findByIdForUpdate(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));

        // 3. Tìm CartItem
        CartItem cartItem = cartItemRepository.findByCartAndSku(cart, sku)
                .orElseThrow(() -> new ResourceNotFoundException("Item không tồn tại trong giỏ"));

        int quantityInCart = cartItem.getQuantity();

        // 4. Tìm CartReservation (có thể null nếu đã expire)
        CartReservation cartReservation =
                cartReservationRepository.findByCartAndSku(cart, sku).orElse(null);

        if (cartReservation != null) {

            // 5. Trả lại stock đã giữ cho cart
            sku.setCartReservedQuantity(
                    sku.getCartReservedQuantity() - cartReservation.getQuantity()
            );

            // Safety check (tránh âm stock do bug)
            if (sku.getCartReservedQuantity() < 0) {
                sku.setCartReservedQuantity(0);
            }

            // 6. Xóa reservation
            cartReservationRepository.delete(cartReservation);
        }

        // 7. Xóa cart item
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        // 8. Lưu SKU & Cart
        skuRepository.save(sku);
        cartRepository.save(cart);

        return MappingDto.cartToResponseDTO(cart);
    }


    /* Xem giỏ hàng */
    @Transactional()
    @Override
    public CartResponseDTO getCart(String cartToken) {
        return MappingDto.cartToResponseDTO(getOrCreateCart(cartToken));
    }


//    Chưa xóa CartItem, Chỉ trả stock
    @Scheduled(fixedDelay = 60000)
    @Transactional
    @Override
    public void releaseExpiredCartReservations() {

        List<CartReservation> expired =
                cartReservationRepository.findExpired(LocalDateTime.now());

        for (CartReservation r : expired) {

            ProductSku sku = skuRepository
                    .findByIdForUpdate(r.getSku().getId())
                    .orElseThrow();

            sku.setCartReservedQuantity(
                    sku.getCartReservedQuantity() - r.getQuantity()
            );

            skuRepository.save(sku);
            cartReservationRepository.delete(r);
        }
    }


}

