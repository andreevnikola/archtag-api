package com.andreev.archtag.user.config;

import com.andreev.archtag.global.exception.ManuallyThrowedException;
import com.andreev.archtag.user.domain.authentication.UserEntity;
import com.andreev.archtag.user.services.authentication.JwtService;
import com.andreev.archtag.user.services.authentication.UserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final String[] UNVALIDATED_ROUTES = {
            "/api/auth/resend-verification",
            "/api/auth/verify-email",
    };

    @Override
    @SneakyThrows
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String uuid = null;
        try {
            uuid = jwtService.extractUuid(jwt);
        } catch (
                ExpiredJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    new ManuallyThrowedException(
                            "Token has expired!",
                            HttpStatus.UNAUTHORIZED
                    ).getExceptionAsJson()
            );
            return;
        }

        if (uuid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserEntity userEntityDetails = this.userDetailsService.getUserByUuid(uuid);

            if (!jwtService.isTokenValid(jwt, userEntityDetails)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        new ManuallyThrowedException(
                                "Невалиден токен!",
                                HttpStatus.UNAUTHORIZED
                        ).getExceptionAsJson()
                );
                return;
            }

            if (!userEntityDetails.getIsVerified() && !Arrays.stream(UNVALIDATED_ROUTES).anyMatch(request.getRequestURI()::contains)) {
                response.setStatus(HttpStatus.CONFLICT.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        new ManuallyThrowedException(
                                "Потребителят не е потвърдил имейла си!",
                                HttpStatus.CONFLICT
                        ).getExceptionAsJson()
                );
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEntityDetails,
                    null,
                    userEntityDetails.getAuthorities()
            );
            authToken.setDetails(
                    userEntityDetails
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }
}
