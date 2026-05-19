package com.rwms.user.repository;

import com.rwms.user.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User persistence operations.
 * Extend JpaRepository<User, Long> once Spring Data JPA dependency is added.
 */
public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(Long id);

    User save(User user);

    void deleteById(Long id);
}
