package fsa.fresher.ks.ecommerce.controller;

import fsa.fresher.ks.ecommerce.model.dto.response.CartResponseDTO;
import fsa.fresher.ks.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponseDTO getCart(@RequestHeader("Cart-Token") String cartToken) {
        return cartService.getCart(cartToken);
    }

    @PostMapping()
    public CartResponseDTO addToCart(
            @RequestHeader("Cart-Token") String cartToken,
            @RequestParam Long skuId,
            @RequestParam int quantity
    ) {
        return cartService.addToCart(cartToken, skuId, quantity);
    }

    @PutMapping()
    public CartResponseDTO updateQuantity(
            @RequestHeader("Cart-Token") String cartToken,
            @RequestParam Long skuId,
            @RequestParam int quantity
    ) {
        return cartService.updateQuantity(cartToken, skuId, quantity);
    }

    @DeleteMapping()
    public CartResponseDTO removeItem(
            @RequestHeader("Cart-Token") String cartToken,
            @RequestParam Long skuId
    ) {
        return cartService.removeItem(cartToken, skuId);
    }
}
