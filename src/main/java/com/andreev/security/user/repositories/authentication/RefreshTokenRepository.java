package com.andreev.security.user.repositories.authentication;

import com.andreev.security.user.domain.authentication.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    @Transactional
//    @Modifying
//    @Query("DELETE FROM YourEntity e WHERE e.userUuid = :uuid")
    void deleteAllByUserUuid(String uuid);
}
