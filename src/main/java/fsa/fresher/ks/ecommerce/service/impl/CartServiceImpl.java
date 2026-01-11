package fsa.fresher.ks.ecommerce.service.impl;

import fsa.fresher.ks.ecommerce.exception.BadRequestException;
import fsa.fresher.ks.ecommerce.exception.ResourceNotFoundException;
import fsa.fresher.ks.ecommerce.model.dto.MappingDto;
import fsa.fresher.ks.ecommerce.model.dto.response.CartResponseDTO;
import fsa.fresher.ks.ecommerce.model.entity.Cart;
import fsa.fresher.ks.ecommerce.model.entity.CartItem;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;
import fsa.fresher.ks.ecommerce.repository.CartItemRepository;
import fsa.fresher.ks.ecommerce.repository.CartRepository;
import fsa.fresher.ks.ecommerce.repository.ProductSkuRepository;
import fsa.fresher.ks.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductSkuRepository skuRepository;

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
    public CartResponseDTO addToCart(String cartToken, Long skuId, int quantity) {
        ProductSku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));
        Cart cart = getOrCreateCart(cartToken);
        // lấy số lượng có thể dùng của sku
        int availableStock = sku.getAvailableStock();

        CartItem cartItem = cartItemRepository.findByCartAndSku(cart, sku)
                .orElse(null);

        int newQuantity = quantity;
        // nếu sản phẩm có trong giỏ hàng rồi
        if (cartItem != null) {
            newQuantity = cartItem.getQuantity() + quantity;
            // nếu thêm số lượng mà tổng số bằng 0 thì xóa khỏi cart luôn
            if (newQuantity <= 0) {
                return removeItem(cartToken, skuId);
            }
        }

        if (newQuantity > availableStock) {
            throw new BadRequestException("Số lượng vượt quá tồn kho");
        }

        // nếu sản phẩm chưa có trong giỏ
        if (cartItem == null) {
            if(quantity <= 0){
                throw new BadRequestException("Số lượng sản phẩm không hợp lệ");
            }
            // khởi tạo cart item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setSku(sku);
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
        } else {
            // cập nhật số lượng mới cho item đã có
            cartItem.setQuantity(newQuantity);
        }
        return MappingDto.cartToResponseDTO(cartRepository.save(cart));
    }

    /* Cập nhật số lượng (tăng / giảm), thay thế quantity vào item đã tồn tại, không cộng như ở phần add */
    @Override
    public CartResponseDTO updateQuantity(String cartToken, Long skuId, int quantity) {
        if (quantity <= 0) {
            return removeItem(cartToken, skuId);
        }

        Cart cart = getOrCreateCart(cartToken);
        ProductSku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));

        CartItem cartItem = cartItemRepository.findByCartAndSku(cart, sku)
                .orElseThrow(() -> new ResourceNotFoundException("Item không tồn tại trong giỏ"));

        if (quantity > sku.getAvailableStock()) {
            throw new BadRequestException("Số lượng vượt quá tồn kho");
        }

        cartItem.setQuantity(quantity);
        return MappingDto.cartToResponseDTO(cartRepository.save(cart));
    }

    /* Xóa item khỏi giỏ */
    @Override
    public CartResponseDTO removeItem(String cartToken, Long skuId) {
        Cart cart = getOrCreateCart(cartToken);

        ProductSku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại"));

        CartItem cartItem = cartItemRepository.findByCartAndSku(cart, sku)
                .orElseThrow(() -> new ResourceNotFoundException("Item không tồn tại"));

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return MappingDto.cartToResponseDTO(cartRepository.save(cart));
    }

    /* Xem giỏ hàng */
    @Transactional()
    @Override
    public CartResponseDTO getCart(String cartToken) {
        return MappingDto.cartToResponseDTO(getOrCreateCart(cartToken));
    }
}

