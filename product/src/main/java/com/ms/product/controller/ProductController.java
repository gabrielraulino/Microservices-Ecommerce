package com.ms.product.controller;

import com.ms.product.dto.CreateProductDTO;
import com.ms.product.dto.ProductDTO;
import com.ms.product.dto.ProductQuantity;
import com.ms.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/products")
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {
    private final ProductService service;

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ProductDTO getProduct(@PathVariable Long id) {
        return service.getProduct(id);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public List<ProductDTO> getAllProducts(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.getAllProducts(pageable);
    }

    @PostMapping
    public ProductDTO createProduct(@RequestBody CreateProductDTO productDTO) {
        return service.saveProduct(productDTO);
    }

    @PutMapping("/{id}")
    public ProductDTO updateProduct(@PathVariable Long id, @RequestBody CreateProductDTO productDTO) {
        return service.updateProduct(id, productDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
    }

    @GetMapping("/validate/{id}")
    public ProductDTO updateProductStock(@PathVariable Long id, @RequestBody int stock) {
        return service.updateProductStock(id, stock);
    }

    @PostMapping("/validate-stock")
    @Operation(summary = "Validate stock for multiple products")
    public ResponseEntity<List<ProductDTO>> validateStock(@RequestBody List<ProductQuantity> validateStock) {
        Map<Long, Integer> stockMap = validateStock.stream()
                .collect(Collectors.toMap(ProductQuantity::productId, ProductQuantity::quantity)
        );
        return service.validateProductsStock(stockMap);
    }

    @GetMapping("/find-products")
    @Operation(summary = "Find products by IDs")
    public List<ProductDTO> findProductsByIds(@RequestParam Set<Long> products) {
        return service.findAllProductsByIds(products);
    }

}
