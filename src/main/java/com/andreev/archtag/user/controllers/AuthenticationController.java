package com.andreev.archtag.user.controllers;

import com.andreev.archtag.global.exception.ApiRequestException;
import com.andreev.archtag.user.services.authentication.AuthenticationService;
import com.andreev.archtag.user.services.authentication.JwtService;
import com.andreev.archtag.user.services.authentication.RefreshTokenService;
import com.andreev.archtag.user.dto.authentication.*;
import com.andreev.archtag.user.services.authentication.UserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @SneakyThrows
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest req
    ) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/signin")
    public Mono<ResponseEntity<AuthenticationResponse>> signin(
            @Valid @RequestBody SigninRequest req
    ) {
        Mono<AuthenticationResponse> responseMono = authService.signin(req);

        return responseMono.map(ResponseEntity::ok)
                .onErrorMap(BadCredentialsException.class, e ->
                    new ApiRequestException(HttpStatus.UNAUTHORIZED, "Акаунта Ви не беше намерен! Невалиден email адрес или парола.")
                );
    }

    @PostMapping("/revalidate")
    public ResponseEntity<RevalidateJwtResponse> revalidateToken(
            @RequestBody RevalidateJwtRequest req
    ) {
        try {
            final CompletableFuture<String> jwtFuture = refreshTokenService.revalidateJwt(req.getRefreshToken());

            final String jwt = jwtFuture.get();

            final RevalidateJwtResponse jwtResponse = RevalidateJwtResponse.builder()
                    .token(jwt)
                    .build();

            return ResponseEntity.ok(jwtResponse);

        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-user-data/{token}")
    public ResponseEntity<UserDto> getUserData(
            @PathVariable String token
    ) {

        if (jwtService.isTokenValid(token) == false) {
            throw new ApiRequestException(HttpStatus.UNAUTHORIZED, "Invalid token.");
        }

        return ResponseEntity.ok(userDetailsService.getUserByToken(token));
    }
}
