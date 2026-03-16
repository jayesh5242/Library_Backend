package com.example.Library_backend.repository;

import com.example.Library_backend.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository
        extends JpaRepository<TokenBlacklist, Long> {

    // Check if a token is blacklisted
    boolean existsByToken(String token);

    // Clean up expired tokens (run by scheduler)
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenBlacklist t WHERE t.tokenExpiry < :now")
    void deleteExpiredTokens(LocalDateTime now);
}