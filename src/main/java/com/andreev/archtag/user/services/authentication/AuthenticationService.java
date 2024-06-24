package com.andreev.archtag.user.services.authentication;

import com.andreev.archtag.global.exception.ApiRequestException;
import com.andreev.archtag.global.lib.AuthenticationInfo;
import com.andreev.archtag.global.services.EmailService;
import com.andreev.archtag.global.utils.ConfigUtility;
import com.andreev.archtag.user.domain.authentication.RefreshTokenEntity;
import com.andreev.archtag.user.domain.authentication.Role;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.dto.authentication.*;
import com.andreev.archtag.user.repositories.authentication.RefreshTokenRepository;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import com.andreev.archtag.global.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
    private final ConfigUtility configUtility;

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

        CompletableFuture<AuthenticationResponse> response = refreshTokenCombinedFuture.thenApplyAsync(refreshTokenCombined -> AuthenticationResponse.builder()
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

        final String verificationCode = UUID.randomUUID().toString();
        userRepo.setVerificationCodeByEmail(email, verificationCode);

        emailService.send(
                email,
                "Потвърдете имейла си",
                configUtility.getProperty("webapp.url") + "/auth/verify-email/" + verificationCode
        );

        return true;
    }

    public boolean verifyEmail(String code) {
        String email = authenticationInfo.getUserEntity().getEmail();

        UserEntity user = userRepo.findByEmail(email).orElseThrow();
        if (user.getIsVerified()) {
            return false;
        }

        if (user.getVerificationCode() == null) {
            throw new IllegalArgumentException("No verification code found.");
        }

        System.out.println("USER: ");
        System.out.println(user.toString());

        if (!user.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        userRepo.setVerifiedByEmail(true, email);
        return true;
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        UserEntity user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiRequestException(HttpStatus.BAD_REQUEST, "No user found with this email address."));

        String resetToken = jwtService.generateToken(user);
        // Send the reset token via email
        emailService.send(user.getEmail(), "Password Reset Request",
                "To reset your password, click the link below:\n" +
                        configUtility.getProperty("webapp.url") + "/auth/reset-password?token=" + resetToken);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String userUuid = jwtService.extractUuid(request.getToken());
        UserEntity user = userRepo.findByUuid(userUuid)
                .orElseThrow(() -> new ApiRequestException(HttpStatus.BAD_REQUEST, "Invalid token."));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
    }
}
