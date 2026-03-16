package com.example.Library_backend.repository;


import com.example.Library_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailVerifyToken(String token);
    Optional<User> findByPasswordResetToken(String token);
}