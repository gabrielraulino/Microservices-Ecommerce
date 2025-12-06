package com.ms.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Table(name = "users")
@Getter
@NoArgsConstructor(force = true)
public class User{
    // @Serial
    // private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private final String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private final Role role;

    @Column(name = "email", nullable = false, unique = true)
    private final String email;

    @Column(name = "password", nullable = false)
    private final String password;

    @Column(name = "created_at")
    private final LocalDateTime createdAt;

    @Column(name = "updated_at")
    private final LocalDateTime updatedAt;

}