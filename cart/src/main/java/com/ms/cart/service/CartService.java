package com.ms.cart.service;

import com.ms.cart.client.OrderServiceClient;
import com.ms.cart.client.ProductService;
import com.ms.cart.dto.*;
import com.ms.cart.dto.CreateOrderRequest;
import com.ms.cart.enums.PaymentMethod;
import com.ms.cart.exception.*;
import com.ms.cart.model.Cart;
import com.ms.cart.model.CartItem;
import com.ms.cart.producer.CartProducer;
import com.ms.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CartService {
    private final CartRepository repository;

    private final ProductService productService;

    private final OrderServiceClient orderServiceClient;

    private final CartProducer producer;

    public List<CartDTO> getAllCarts(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::buildCartDTO)
                .getContent();
    }

    // returns cart by user id, or an empty cart if none exists
    public CartDTO getCartUserById(Long id) {
        Cart cart = repository.findCartByUserId(id)
                .orElseGet(() -> new Cart(null, id, LocalDateTime.now(), null));
        return buildCartDTO(cart);
    }

    public CartDTO addOrUpdateItem(Long userId, AddCartItemDTO cartData) {

        var productQuantity = new ProductQuantity(cartData.productId(), cartData.quantity());

        productService.validateProductsStock(List.of(productQuantity));

        Cart cart = repository.findCartByUserId(userId).orElseGet(() -> {
            Cart newCart = new Cart(null, userId, LocalDateTime.now(), null);
            return repository.save(newCart);
        });

        Optional<CartItem> existingItemOpt = cart.getItems()
                .stream()
                .filter(
                        item -> item
                                .getProductId()
                                .equals(cartData.productId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            CartItem updatedItem = existingItem.updateQuantity(cartData.quantity());
            cart.removeItem(existingItem);
            cart.addItem(updatedItem);
        } else {
            CartItem newItem = new CartItem(cart, cartData.productId(), cartData.quantity());
            cart.addItem(newItem);
        }

        // Update updatedAt
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = repository.save(cart);
        return buildCartDTO(savedCart);
    }

    private CartDTO buildCartDTO(Cart cart) {
        List<CartItemDTO> enrichedItems = buildCartItemsDTO(cart.getItems());

        // Calculate total quantity
        int totalQuantity = enrichedItems.stream()
                .mapToInt(CartItemDTO::quantity)
                .sum();

        // Calculate total price
        BigDecimal totalPrice = enrichedItems.stream()
                .map(CartItemDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(
                cart.getId(),
                cart.getUserId(),
                enrichedItems,
                totalQuantity,
                totalPrice,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private List<CartItemDTO> buildCartItemsDTO(List<CartItem> items) {
        if (items.isEmpty()) {
            return List.of();
        }

        // Extract unique product IDs
        Set<Long> productIds = items.stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());

        // Fetch all products in a single call (avoids N+1)
        Map<Long, InternalProductDTO> productMap = productService.findProductsByIds(List.copyOf(productIds))
                .stream()
                .collect(Collectors.toMap(InternalProductDTO::id, Function.identity()));

        // Iterate over original items to maintain order and process all
        // Filter items whose products were not found
        return items.stream()
                .filter(item -> productMap.containsKey(item.getProductId()))
                .map(item -> {
                    InternalProductDTO product = productMap.get(item.getProductId());
                    BigDecimal subtotal = product.price()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    
                    return new CartItemDTO(
                            item.getId(),
                            item.getProductId(),
                            product.name(),
                            product.price(),
                            item.getQuantity(),
                            subtotal
                    );
                })
                .toList();
    }

    @Transactional
    public ResponseEntity<OrderDTO> checkout(Long userId, PaymentMethod paymentMethod) {
        log.info("Starting checkout for user: {}", userId);

        Cart cart = findCartByUser(userId);
        validateCartEmpty(cart);

        // 1. Validate stock (synchronous)
        Map<Long, Integer> productQuantities = cart.getItems().stream()
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));

        List<ProductQuantity> productQuantitiesList = productQuantities.entrySet().stream()
                .map(entry -> new ProductQuantity(entry.getKey(), entry.getValue()))
                .toList();

        productService.validateProductsStock(productQuantitiesList);
        log.info("Stock validated successfully for {} products", productQuantitiesList.size());

        // 2. Create order (synchronous - ensures success)
        List<CreateOrderRequest.CreateOrderItemRequest> items = cart.getItems().stream()
                .map(item -> new CreateOrderRequest.CreateOrderItemRequest(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .toList();

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                userId,
                items,
                paymentMethod.name()
        );

        OrderDTO order = orderServiceClient.createOrder(orderRequest);
        log.info("Order created successfully. ID: {}", order.id());

        // 3. Publish event to update stock (asynchronous)
        UpdateStockEvent stockEvent = new UpdateStockEvent(
                cart.getId(),
                userId,
                productQuantities
        );
        producer.publishUpdateStockEvent(stockEvent);
        log.info("UpdateStockEvent published for cart: {}", cart.getId());

        // 4. Clear cart items (only if order was created successfully)
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        repository.save(cart);
        log.info("Cart cleared for user: {}", userId);

        return ResponseEntity.ok(order);
    }

    private Cart findCartByUser(Long userId) {
        return repository.findCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "user_id", userId));
    }

    private void validateCartEmpty(Cart cart) {
        if (cart.getItems().isEmpty()) {
            throw new InvalidOperationException("checkout", "cart is empty");
        }
    }

}
