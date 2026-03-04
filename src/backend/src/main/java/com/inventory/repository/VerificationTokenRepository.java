package com.inventory.repository;

import com.inventory.enums.TokenType;
import com.inventory.model.User;
import com.inventory.model.VerificationToken;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByTokenAndType(String token, TokenType type);

    void deleteByUserAndType(User user, TokenType type);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
