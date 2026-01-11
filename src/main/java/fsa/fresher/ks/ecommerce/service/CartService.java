package fsa.fresher.ks.ecommerce.service;

import fsa.fresher.ks.ecommerce.model.dto.response.CartResponseDTO;

public interface CartService {

    CartResponseDTO addToCart(String cartToken, Long skuId, int quantity);

    CartResponseDTO updateQuantity(String cartToken, Long skuId, int quantity);

    CartResponseDTO removeItem(String cartToken, Long skuId);

    CartResponseDTO getCart(String cartToken);
}

