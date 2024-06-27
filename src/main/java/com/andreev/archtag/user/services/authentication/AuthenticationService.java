package com.andreev.archtag.user.services.authentication;

import com.andreev.archtag.global.exception.ApiRequestException;
import com.andreev.archtag.global.lib.AuthenticationInfo;
import com.andreev.archtag.global.services.EmailService;
import com.andreev.archtag.global.utils.ConfigUtility;
import com.andreev.archtag.user.domain.authentication.RefreshTokenEntity;
import com.andreev.archtag.user.domain.authentication.Role;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.dto.authentication.AuthenticationResponse;
import com.andreev.archtag.user.dto.authentication.ForgotPasswordRequest;
import com.andreev.archtag.user.dto.authentication.ForgottenPassResponse;
import com.andreev.archtag.user.dto.authentication.RegisterRequest;
import com.andreev.archtag.user.dto.authentication.ResetPasswordRequest;
import com.andreev.archtag.user.dto.authentication.SigninRequest;
import com.andreev.archtag.user.dto.authentication.UpdateAccountRequest;
import com.andreev.archtag.user.repositories.authentication.RefreshTokenRepository;
import com.andreev.archtag.user.repositories.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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
                .verificationCode(generateVerificationCode())
                .verificationCodeExpiry(LocalDateTime.now().plusHours(24))
                .build();

        userRepo.save(user);

        sendVerificationEmail(user);

        String jwt = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user.getUuid());

        return Mono.just(AuthenticationResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .build());
    }

    public Mono<AuthenticationResponse> signin(SigninRequest req) {
        CompletableFuture<Void> authenticationFuture = authenticateUser(req.getEmail(), req.getPassword());

        UserEntity user = userRepo.findByEmail(req.getEmail()).orElseThrow();

        CompletableFuture<Void> ifNeededdeleteRefreshTokenFuture = ifNeededdeleteRefreshTokenFuture(user.getUuid());
        CompletableFuture<String> refreshTokenFuture = CompletableFuture.supplyAsync(() -> refreshTokenService.generateRefreshToken(user.getUuid()));

        CompletableFuture<String> refreshToken = ifNeededdeleteRefreshTokenFuture.thenCombine(refreshTokenFuture, (aVoid, refreshTokenFromFuture) -> refreshTokenFromFuture);

        String jwt = jwtService.generateToken(user);

        CompletableFuture<AuthenticationResponse> response = authenticationFuture.thenCombineAsync(refreshToken,
                (aVoid, refreshTokenValue) -> AuthenticationResponse.builder()
                        .token(jwt)
                        .refreshToken(refreshTokenValue)
                        .build()
        );

        return Mono.fromFuture(response);
    }

    public boolean resendVerification() {
        String email = authenticationInfo.getUserEntity().getEmail();

        UserEntity user = userRepo.findByEmail(email).orElseThrow();
        if (user.getIsVerified()) {
            return false;
        }

        final String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
        userRepo.save(user);

        sendVerificationEmail(user);

        return true;
    }

    private void sendVerificationEmail(UserEntity user) {
        String verificationUrl = configUtility.getProperty("webapp.url") + "/auth/verify-email/?code=" + user.getVerificationCode();
        emailService.send(user.getEmail(), "Verify Your Email", "Please verify your email by clicking the link: " + verificationUrl);
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString();
    }

    public boolean verifyEmail(String code) {
        UserEntity user = userRepo.findByVerificationCode(code)
                .orElseThrow(() -> new ApiRequestException(HttpStatus.BAD_REQUEST, "Invalid verification code."));
        if (user.getIsVerified()) {
            throw new ApiRequestException(HttpStatus.CONFLICT, "Email is already verified.");
        }
        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST, "Verification code has expired.");
        }
        user.setIsVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepo.save(user);
        return true;
    }

    public ForgottenPassResponse forgotPassword(ForgotPasswordRequest request) {
        UserEntity user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiRequestException(HttpStatus.BAD_REQUEST, "No user found with this email address."));

        user.setResetPasswordCode(generateVerificationCode());
        user.setResetPasswordCodeExpiry(LocalDateTime.now().plusHours(24));
        userRepo.save(user);

        sendResetPasswordEmail(user);
        return new ForgottenPassResponse(true, "Password reset email sent.");
    }

    private void sendResetPasswordEmail(UserEntity user) {
        String resetUrl = configUtility.getProperty("webapp.url") + "/auth/reset-password?code=" + user.getResetPasswordCode();
        emailService.send(user.getEmail(), "Password Reset Request", "To reset your password, click the link: " + resetUrl);
    }

    public ForgottenPassResponse resetPassword(ResetPasswordRequest request) {
        UserEntity user = userRepo.findByResetPasswordCode(request.getCode())
                .orElseThrow(() -> new ApiRequestException(HttpStatus.BAD_REQUEST, "Invalid reset code."));

        if (user.getResetPasswordCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST, "Reset code has expired.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordCode(null);
        user.setResetPasswordCodeExpiry(null);
        userRepo.save(user);
        return new ForgottenPassResponse(true, "Password has been reset successfully.");
    }

    public void deleteAccount(String email, String authToken) {
        UserEntity authUser = getUserFromToken(authToken);
        if (!authUser.getEmail().equals(email)) {
            throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "You can only delete your own account.");
        }
        userRepo.deleteByEmail(email);
    }

    public void updateAccount(UpdateAccountRequest request, String authToken) {
        UserEntity authUser = getUserFromToken(authToken);
        if (!authUser.getEmail().equals(request.getEmail())) {
            throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "You can only update your own account.");
        }

        if (request.getFirstname() != null && !request.getFirstname().isEmpty()) {
            authUser.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null && !request.getLastname().isEmpty()) {
            authUser.setLastname(request.getLastname());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            authUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepo.save(authUser);
    }

    private UserEntity getUserFromToken(String token) {
        String userUuid = jwtService.extractUuid(token);
        return userRepo.findByUuid(userUuid).orElseThrow(() -> new ApiRequestException(HttpStatus.UNAUTHORIZED, "Invalid token."));
    }

    private CompletableFuture<Void> authenticateUser(String email, String password) {
        return CompletableFuture.runAsync(() -> {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        });
    }

    private CompletableFuture<Void> ifNeededdeleteRefreshTokenFuture(String userUuid) {
        return CompletableFuture.runAsync(() -> {
            List<RefreshTokenEntity> refreshTokens = refreshTokenRepo.findByUserUuid(userUuid);
            if (refreshTokens.size() > 4) {
                refreshTokenRepo.deleteByRefreshToken(refreshTokens.get(0).getRefreshToken());
            }
        });
    }
}
