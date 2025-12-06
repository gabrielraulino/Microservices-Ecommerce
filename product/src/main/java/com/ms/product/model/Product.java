package com.ms.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Table(name = "products")
@Getter
@NoArgsConstructor(force = true)
public class Product  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column(name = "name")
    private final String name;

    @Column(name = "description")
    private final String description;

    @Column(name = "price")
    private final BigDecimal price;

    @Column(name = "stock")
    private final int stock;

    @Column(name = "created_at")
    private final LocalDateTime createdAt;

    @Column(name = "updated_at")
    private final LocalDateTime updatedAt;
}
