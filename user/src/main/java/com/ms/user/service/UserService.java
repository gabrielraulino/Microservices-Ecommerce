package com.ms.user.service;

import com.ms.user.dto.UserCreateDTO;
import com.ms.user.dto.UserDTO;
import com.ms.user.exception.DuplicateResourceException;
import com.ms.user.exception.ResourceNotFoundException;
import com.ms.user.model.Role;
import com.ms.user.model.User;
import com.ms.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserDTO findById(Long id) {
        return repository.findById(id).map(UserDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public List<UserDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(UserDTO::fromEntity)
                .getContent();
    }

    public UserDTO saveUser(UserCreateDTO user) {
        if (repository.findByEmail(user.email()).isPresent()) {
            throw new DuplicateResourceException("User already exists");
        }
        User newUser = new User(
                null,
                user.name(),
                Role.USER,
                user.email(),
                passwordEncoder.encode(user.password()),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        return UserDTO.fromEntity(repository.save(newUser));
    }

    public UserDTO updateUser(Long id, UserCreateDTO user) {
        User existingUser = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        User updatedUser = new User(
                existingUser.getId(),
                user.name(),
                existingUser.getRole(),
                user.email(),
                passwordEncoder.encode(user.password()),
                existingUser.getCreatedAt(),
                LocalDateTime.now()
        );

        return UserDTO.fromEntity(repository.save(updatedUser));
    }

    public void deleteUser(Long id) {
        findById(id);
        repository.deleteById(id);
    }

}
