package com.andreev.archtag.user.repositories.authentication;

import com.andreev.archtag.user.domain.authentication.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    List<RefreshTokenEntity> findByUserUuid(String uuid);

    @Transactional
    void deleteByRefreshToken(String refreshToken);

    @Transactional
    void deleteAllByUserUuid(String uuid);
}
