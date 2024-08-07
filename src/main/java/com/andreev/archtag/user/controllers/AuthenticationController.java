package com.andreev.archtag.user.controllers;

import com.andreev.archtag.global.exception.ApiRequestException;
import com.andreev.archtag.user.dto.authentication.*;
import com.andreev.archtag.user.services.authentication.AuthenticationService;
import com.andreev.archtag.user.services.authentication.JwtService;
import com.andreev.archtag.user.services.authentication.RefreshTokenService;
import com.andreev.archtag.user.services.authentication.UserDetailsService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest req
    ) {
        Mono<AuthenticationResponse> responseMono = null;
        try {
            responseMono = authService.register(req);
        } catch (DataIntegrityViolationException e) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST, "Потребител с този email вече съществува!");
        }

        return responseMono.map(ResponseEntity::ok);
    }

    @PostMapping("/signin")
    public Mono<ResponseEntity<AuthenticationResponse>> signin(
            @Valid @RequestBody SigninRequest req
    ) {
        Mono<AuthenticationResponse> responseMono = null;
        try {
            responseMono = authService.signin(req);
        } catch (NoSuchElementException e) {
            throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "Акаунта Ви не беше намерен! Невалиден email адрес или парола.");
        }

        assert responseMono != null;
        return responseMono.map(ResponseEntity::ok)
                .onErrorMap(BadCredentialsException.class, e ->
                        new ApiRequestException(HttpStatus.UNAUTHORIZED, "Акаунта Ви не беше намерен! Невалиден email адрес или парола.")
                );
    }

    @PostMapping("/revalidate")
    public ResponseEntity<RevalidateJwtResponse> revalidateToken(
            @RequestBody RevalidateJwtRequest req
    ) throws Exception {
        try {
            final String jwt = refreshTokenService.revalidateJwt(req.getRefreshToken());

            final RevalidateJwtResponse jwtResponse = RevalidateJwtResponse.builder()
                    .token(jwt)
                    .build();

            return ResponseEntity.ok(jwtResponse);

        } catch (NoSuchElementException e) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST, "The refresh token was not found.");
        } catch (Exception e) {
            throw new ApiRequestException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/get-user-data/{token}")
    public ResponseEntity<UserDto> getUserData(@PathVariable String token) {
        try {
            if (!jwtService.isTokenValid(token)) {
                throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "Invalid token.");
            }

            return ResponseEntity.ok(userDetailsService.getUserByToken(token));
        } catch (StripeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null);
        } catch (ApiRequestException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(
    ) {
        try {
            boolean hasBeenAlreadyValidated = authService.resendVerification();
            if (!hasBeenAlreadyValidated) {
                throw new ApiRequestException(HttpStatus.CONFLICT, "Верификацията вече е направена успешно.");
            }
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST, "Потребител с този имейл адрес не бе намерен.");
        }
    }

    @GetMapping("/verify-email/{code}")
    public ResponseEntity<Void> verifyEmail(
            @PathVariable String code
    ) {
        try {
            final boolean isVerified = authService.verifyEmail(code);
            if (!isVerified) {
                throw new ApiRequestException(HttpStatus.CONFLICT, "Верификацията вече е направена успешно.");
            }
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST, "Потребител с този имейл не бе намерен.");
        } catch (IllegalArgumentException e) {
            throw new ApiRequestException(HttpStatus.BAD_REQUEST, "Невалиден линк.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgottenPassResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            ForgottenPassResponse response = authService.forgotPassword(request);
            return ResponseEntity.ok(response);
        } catch (ApiRequestException e) {
            return ResponseEntity.status(e.getStatus()).body(new ForgottenPassResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ForgottenPassResponse(false, "Възникна неочаквана грешка."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ForgottenPassResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            ForgottenPassResponse response = authService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (ApiRequestException e) {
            return ResponseEntity.status(e.getStatus()).body(new ForgottenPassResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ForgottenPassResponse(false, "Възникна неочаквана грешка."));
        }
    }
}
