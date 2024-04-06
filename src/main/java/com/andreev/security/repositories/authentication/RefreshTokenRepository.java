package com.andreev.security.repositories.authentication;

import com.andreev.security.domain.authentication.RefreshTokenEntity;
import com.andreev.security.domain.authentication.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    @Transactional
//    @Modifying
//    @Query("DELETE FROM YourEntity e WHERE e.userUuid = :uuid")
    void deleteAllByUserUuid(String uuid);
}
