package com.ms.cart.client;

import java.util.List;

import com.ms.cart.dto.ProductQuantity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.ms.cart.dto.InternalProductDTO;

import org.springframework.http.ResponseEntity;

@FeignClient(name = "product-service", url = "${product-service.url}")
public interface ProductService {

    /**
     * Valida estoque de um produto individual
     */
    @GetMapping("/products/{productId}")
    InternalProductDTO getProduct(@PathVariable("productId") Long productId);

    /**
     * Valida estoque de um produto individual
     * Nota: Este endpoint pode não existir no product service,
     * nesse caso use getProduct e valide manualmente
     */
    @GetMapping("/products/validate/{productId}")
    ResponseEntity<InternalProductDTO> validateProductStock(
            @PathVariable("productId") Long productId,
            @RequestParam("stock") int quantity);

    
    @PostMapping("/products/validate-stock")
    ResponseEntity<List<InternalProductDTO>> validateProductsStock(@RequestBody List<ProductQuantity> validateStock);

    /**
     * Busca produtos por IDs
     * Usa List ao invés de Set porque Feign tem melhor suporte para List em query params
     */
    @GetMapping("/products/find-products")
    List<InternalProductDTO> findProductsByIds(@RequestParam("products") List<Long> ids);
}
