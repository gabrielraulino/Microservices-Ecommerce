package com.ms.product.service;

import com.ms.product.dto.CreateProductDTO;
import com.ms.product.dto.ProductDTO;
import com.ms.product.dto.ProductQuantity;
import com.ms.product.dto.OrderCancelledEvent;
import com.ms.product.exception.InsufficientStockException;
import com.ms.product.exception.ResourceNotFoundException;
import com.ms.product.exception.ValidationException;
import com.ms.product.model.Product;
import com.ms.product.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository repository;

    public ProductDTO getProduct(Long id) {
        return repository.findById(id)
                .map(ProductDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public List<ProductDTO> getAllProducts(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ProductDTO::fromEntity)
                .getContent();
    }

    public ProductDTO saveProduct(CreateProductDTO productDTO) {
        Product product = new Product(
                null,
                productDTO.name(),
                productDTO.description(),
                productDTO.price(),
                productDTO.stock(),
                LocalDateTime.now(),
                null);
        return ProductDTO.fromEntity(repository.save(product));
    }

    public ProductDTO updateProduct(Long id, CreateProductDTO productDTO) {
        Product existingProduct = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        Product updatedProduct = new Product(
                existingProduct.getId(),
                productDTO.name(),
                productDTO.description(),
                productDTO.price(),
                productDTO.stock(),
                existingProduct.getCreatedAt(),
                LocalDateTime.now());
        return ProductDTO.fromEntity(repository.save(updatedProduct));
    }

    public void deleteProduct(Long id) {
        repository.delete(getProductById(id));
    }

    public ProductDTO updateProductStock(Long id, int newStock) {
        validateStock(newStock);

        Product existingProduct = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Product updatedProduct = new Product(
                existingProduct.getId(),
                existingProduct.getName(),
                existingProduct.getDescription(),
                existingProduct.getPrice(),
                newStock,
                existingProduct.getCreatedAt(),
                LocalDateTime.now());
        return ProductDTO.fromEntity(repository.save(updatedProduct));
    }

    private Product updateProductStock(Product product, int newStock) {
        validateStock(newStock);

        Product updatedProduct = new Product(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                newStock,
                product.getCreatedAt(),
                LocalDateTime.now());
        return repository.save(updatedProduct);
    }

    public List<ProductDTO> findAllProductsByIds(Set<Long> productIds) {
        return repository.findAllByIdIn(productIds).stream()
                .map(ProductDTO::fromEntity)
                .toList();
    }

    public ResponseEntity<List<ProductDTO>> validateProductsStock(Map<Long, Integer> productQuantities) {
        // receive to map of productId -> requiredQuantity to avoid multiple DB calls
        // (n+1 problem)

        List<Product> products = repository.findAllByIdIn(productQuantities.keySet());

        // fails in verify stock to avoid partial updates
        products.forEach(product -> {
            int requiredQuantity = productQuantities.get(product.getId());
            if (product.getStock() < requiredQuantity) {
                log.error("Product {} has stock less than required quantity", product.getId());
                throw new InsufficientStockException(product.getName(), requiredQuantity, product.getStock());
            }
        });

        return ResponseEntity.ok(products.stream().map(ProductDTO::fromEntity).toList());

    }

    public List<ProductDTO> decrementProducts(List<ProductQuantity> productQuantities) {
        Map<Long, Integer> productQuantitiesMap = productQuantities.stream()
                .collect(Collectors.toMap(ProductQuantity::productId, ProductQuantity::quantity));


        List<Product> products = repository.findAllByIdIn(productQuantitiesMap.keySet());

        products.forEach(product -> {
            int requiredQuantity = productQuantitiesMap.get(product.getId());
            if (product.getStock() < requiredQuantity) {
                throw new InsufficientStockException(product.getName(), requiredQuantity, product.getStock());
            }
        });

        List<Product> updatedProducts = products.stream().map(product -> {
            int requiredQuantity = productQuantitiesMap.get(product.getId());
            int newStock = product.getStock() - requiredQuantity;
            log.info("Stock updated for product {}: {} -> {} (Decremented by: {})",
                    product.getId(), product.getStock(), newStock, requiredQuantity);
            return updateProductStock(product, newStock);
        }).toList();

        return repository.saveAll(updatedProducts).stream().map(ProductDTO::fromEntity).toList();

    }

    public List<ProductDTO> incrementProducts(List<ProductQuantity> productQuantities) {
        Map<Long, Integer> productQuantitiesMap = productQuantities.stream()
                .collect(Collectors.toMap(ProductQuantity::productId, ProductQuantity::quantity));

        List<Product> products = repository.findAllByIdIn(productQuantitiesMap.keySet());

        List<Product> updatedProducts = products.stream().map(product -> {
            int quantityToAdd = productQuantitiesMap.get(product.getId());
            int newStock = product.getStock() + quantityToAdd;
            log.info("Stock restored for product {}: {} -> {} (Incremented by: {})",
                    product.getId(), product.getStock(), newStock, quantityToAdd);
            return updateProductStock(product, newStock);
        }).toList();

        return repository.saveAll(updatedProducts).stream().map(ProductDTO::fromEntity).toList();
    }

    @EventListener
    public void onOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Processing stock restoration for cancelled order. Order: {}, User: {}",
                event.orderId(), event.userId());

        Set<Long> productIds = event.items().stream().map(OrderCancelledEvent.CancelledItem::productId)
                .collect(Collectors.toSet());

        List<Product> products = repository.findAllByIdIn(productIds);

        for (OrderCancelledEvent.CancelledItem item : event.items()) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(item.productId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.productId()));

            int oldStock = product.getStock();
            int newStock = product.getStock() + item.quantity();

            updateProductStock(product, newStock);

            log.info("Stock restored for product {}: {} -> {} (Incremented by: {})",
                    product.getId(), oldStock, newStock, item.quantity());
        }

        log.info("Stock restored successfully for {} products in order {}",
                event.items().size(), event.orderId());

    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new ValidationException("stock", String.valueOf(stock), "cannot be negative");
        }
    }

    private Product getProductById(Long product) {
        return repository.findById(product)
                .orElseThrow(() -> new ResourceNotFoundException("Product", product));
    }
}
