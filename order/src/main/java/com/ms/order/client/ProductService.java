package com.ms.order.client;

import com.ms.order.dto.InternalProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@FeignClient(name = "product")
public interface ProductService {

    @GetMapping("/products/find-products")
    List<InternalProductDTO> findProductsByIds(@RequestParam Set<Long> products);
}
