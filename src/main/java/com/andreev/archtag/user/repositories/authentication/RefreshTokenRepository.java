package com.andreev.archtag.user.repositories.authentication;

import com.andreev.archtag.user.domain.authentication.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);

    Collection<RefreshTokenEntity> findByUserUuid(String uuid);

    @Transactional
    void deleteByRefreshToken(String refreshToken);

    @Transactional
//    @Modifying
//    @Query("DELETE FROM YourEntity e WHERE e.userUuid = :uuid")
    void deleteAllByUserUuid(String uuid);
}