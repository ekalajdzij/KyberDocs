package com.kyberdocs.docs.auth;
import com.kyberdocs.docs.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUser(User user);
    Optional<RefreshToken> findByTokenAndUser(String token, User user);
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
}