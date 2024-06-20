package com.andreev.archtag.user.services.authentication;

import com.andreev.archtag.global.lib.AuthenticationInfo;
import com.andreev.archtag.global.services.EmailService;
import com.andreev.archtag.user.domain.authentication.RefreshTokenEntity;
import com.andreev.archtag.user.domain.authentication.Role;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.dto.authentication.AuthenticationResponse;
import com.andreev.archtag.user.dto.authentication.RegisterRequest;
import com.andreev.archtag.user.dto.authentication.SigninRequest;
import com.andreev.archtag.user.repositories.authentication.RefreshTokenRepository;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    @Autowired
    private final AuthenticationInfo authenticationInfo;

    public Mono<AuthenticationResponse> register(RegisterRequest req) {
        UserEntity user = UserEntity.builder()
                .firstname(req.getFirstname())
                .lastname(req.getLastname())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .isVerified(false)
                .isBanned(false)
                .build();

        CompletableFuture<Void> saveUserFuture = CompletableFuture.runAsync(() -> userRepo.save(user));
        CompletableFuture<String> refreshTokenFuture = CompletableFuture.supplyAsync(() -> refreshTokenService.generateRefreshToken(user.getUuid()));
        CompletableFuture<String> refreshTokenCombinedFuture = saveUserFuture.thenCombine(refreshTokenFuture, (aVoid, refreshTokenFromFuture) -> refreshTokenFromFuture);

        String jwt = jwtService.generateToken(user);

        CompletableFuture<AuthenticationResponse> response = refreshTokenCombinedFuture.thenApplyAsync(refreshTokenCombined ->  AuthenticationResponse.builder()
                .token(jwt)
                .refreshToken(refreshTokenCombined)
                .build());

        return Mono.fromFuture(response);
    }

    private CompletableFuture authenticateUser(String email, String password) {
        return CompletableFuture.runAsync(() -> {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );
        });
    }

    private CompletableFuture ifNeededdeleteRefreshTokenFuture(String userUuid) {
        return CompletableFuture.runAsync(() -> {
            Collection<RefreshTokenEntity> refreshTokens = refreshTokenRepo.findByUserUuid(userUuid);
            if (refreshTokens.size() > 4) {
                refreshTokenRepo.deleteByRefreshToken(refreshTokens.iterator().next().getRefreshToken());
            }
        });
    }

    public Mono<AuthenticationResponse> signin(SigninRequest req) {
        CompletableFuture<Void> authenticationFuture = this.authenticateUser(req.getEmail(), req.getPassword());

        UserEntity user = userRepo.findByEmail(req.getEmail()).orElseThrow();

        CompletableFuture<Void> ifNeededdeleteRefreshTokenFuture = ifNeededdeleteRefreshTokenFuture(user.getUuid());
        CompletableFuture<String> refreshTokenFuture = CompletableFuture.supplyAsync(() -> refreshTokenService.generateRefreshToken(user.getUuid()));

        CompletableFuture<String> refreshToken = ifNeededdeleteRefreshTokenFuture.thenCombine(refreshTokenFuture, (aVoid, refreshTokenFromFuture) -> refreshTokenFromFuture);

        String jwt = jwtService.generateToken(user);

        CompletableFuture<AuthenticationResponse> response = authenticationFuture.thenCombineAsync(refreshToken,
                (aVoid, refreshTokenValue) -> {
                    return AuthenticationResponse.builder()
                            .token(jwt)
                            .refreshToken(refreshTokenValue)
                            .build();
                }
        );

        return Mono.fromFuture(response);
    }

    public boolean resendVerification() {
        String email = authenticationInfo.getUserEntity().getEmail();

        UserEntity user = userRepo.findByEmail(email).orElseThrow();
        if (user.getIsVerified()) {
            return false;
        }

        emailService.send(
                email,
                "Потвърдете имейла си",
         ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/verify-email"
        );

        return true;
    }
}
