package com.andreev.archtag.user.repositories.authentication;

import com.andreev.archtag.user.domain.authentication.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUuid(String uuid);

    @Modifying
    @Query("UPDATE UserEntity u SET u.isVerified = :verified WHERE u.email = :email")
    void setVerifiedByEmail(@Param("verified") boolean verified, @Param("email") String email);

    @Modifying
    @Query("UPDATE UserEntity u SET u.verificationCode = :verificationCode WHERE u.email = :email")
    void setVerificationCodeByEmail(@Param("verificationCode") String verificationCode, @Param("email") String email);

    @Transactional
    void deleteByEmail(String email);
}
