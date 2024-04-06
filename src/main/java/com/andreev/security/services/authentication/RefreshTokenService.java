package com.andreev.security.services.authentication;

import com.andreev.security.domain.authentication.RefreshTokenEntity;
import com.andreev.security.domain.authentication.UserEntity;
import com.andreev.security.repositories.authentication.RefreshTokenRepository;
import com.andreev.security.repositories.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public String generateRefreshToken(String userUuid) {
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .userUuid(userUuid)
                .validUntil(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30))
                .build();
        return refreshTokenRepository.save(refreshTokenEntity).getRefreshToken();
    }

    public String revalidateJwt(String refreshToken) throws Exception {
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow();
        if (refreshTokenEntity.getValidUntil().before(new Date())) throw new Exception("Refresh token has expired");
        UserEntity user = userRepository.findByUuid(refreshTokenEntity.getUserUuid()).orElseThrow();
        return jwtService.generateToken(user);
    }

    public void deleteAllRefreshTokensForUser(String userUuid) {
        refreshTokenRepository.deleteAllByUserUuid(userUuid);
    }
}
