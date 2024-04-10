package com.andreev.archtag.user.services.authentication;

import com.andreev.archtag.user.domain.authentication.RefreshTokenEntity;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.repositories.authentication.RefreshTokenRepository;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

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
