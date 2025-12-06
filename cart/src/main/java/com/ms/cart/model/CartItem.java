package com.ms.cart.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(force = true)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @JsonBackReference
    private final Cart cart;

    @Column(name = "product_id")
    private final Long productId;

    @Column(name = "quantity")
    private final int quantity;

    public CartItem(Cart cart, Long productId, int quantity) {
        this.id = null;
        this.cart = cart;
        this.productId = productId;
        this.quantity = quantity;
    }

    public CartItem updateQuantity(int newQuantity) {
        return new CartItem(this.id, this.cart, this.productId, newQuantity);
    }

}