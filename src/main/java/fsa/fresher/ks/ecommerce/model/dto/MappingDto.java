package fsa.fresher.ks.ecommerce.model.dto;

import fsa.fresher.ks.ecommerce.model.dto.response.CartItemResponseDTO;
import fsa.fresher.ks.ecommerce.model.dto.response.CartResponseDTO;
import fsa.fresher.ks.ecommerce.model.dto.response.ProductDetailResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.ProductItemResponse;
import fsa.fresher.ks.ecommerce.model.dto.response.ProductSkuResponse;
import fsa.fresher.ks.ecommerce.model.entity.Cart;
import fsa.fresher.ks.ecommerce.model.entity.CartItem;
import fsa.fresher.ks.ecommerce.model.entity.Product;
import fsa.fresher.ks.ecommerce.model.entity.ProductImage;
import fsa.fresher.ks.ecommerce.model.entity.ProductSku;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class MappingDto {

    public static ProductItemResponse productToItemResponse(Product product) {
        BigDecimal minPrice = product.getSkus().stream()
                .map(ProductSku::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = product.getSkus().stream()
                .map(ProductSku::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        boolean inStock = product.getSkus().stream()
                .anyMatch(sku -> sku.getAvailableStock() > 0);

        String firstImage = product.getImages() != null && !product.getImages().isEmpty()
                ? product.getImages().get(0).getUrl()
                : null;

        return new ProductItemResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory().getName(),
                product.getCategory().getSlug(),
                minPrice,
                maxPrice,
                inStock,
                firstImage,
                product.getVideoUrl()
        );
    }

    public static ProductDetailResponse productToDetailResponse(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory().getName(),
                product.getCategory().getSlug(),
                product.getVideoUrl(),
                product.getImages() == null ? List.of() : product.getImages().stream()
                        .map(img -> img.getUrl())
                        .toList(),
                product.getSkus().stream()
                        .map(sku -> new ProductSkuResponse(
                                sku.getId(),
                                sku.getSkuCode(),
                                sku.getSize().name(),
                                sku.getColor().name(),
                                sku.getPrice(),
                                sku.getAvailableStock()
                        ))
                        .toList()
        );
    }


    public static CartResponseDTO cartToResponseDTO(Cart cart) {
        List<CartItemResponseDTO> itemDTOs = cart.getItems()
                .stream()
                .map(MappingDto::toItemDTO)
                .toList();

        BigDecimal total = itemDTOs.stream()
                .map(fsa.fresher.ks.ecommerce.model.dto.response.CartItemResponseDTO::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponseDTO dto = new CartResponseDTO();
        dto.setCartToken(cart.getCartToken());
        dto.setItems(itemDTOs);
        dto.setTotalAmount(total);

        return dto;
    }

    public static CartItemResponseDTO toItemDTO(CartItem item) {
        ProductSku sku = item.getSku();

        BigDecimal subTotal = sku.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setSkuId(sku.getId());
        dto.setProductName(sku.getProduct().getName());
        dto.setSize(sku.getSize());
        dto.setColor(sku.getColor());
        dto.setPrice(sku.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubTotal(subTotal);

        return dto;
    }

}
