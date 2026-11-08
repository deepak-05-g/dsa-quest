package com.dsaquest.auth.controller;

import com.dsaquest.auth.dto.AuthResponse;
import com.dsaquest.auth.dto.LoginRequest;
import com.dsaquest.auth.dto.RegisterRequest;
import com.dsaquest.auth.dto.UserResponse;
import com.dsaquest.auth.service.AuthenticationService;
import com.dsaquest.auth.service.AuthenticationService.IssuedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    static final String REFRESH_COOKIE = "dsa_refresh_token";
    private final AuthenticationService authenticationService;
    private final CsrfTokenRepository csrfTokenRepository;
    private final Duration refreshTokenTtl;
    private final boolean secureCookie;
    private final String sameSite;
    private final String cookieDomain;

    public AuthController(
            AuthenticationService authenticationService,
            CsrfTokenRepository csrfTokenRepository,
            @Value("${spring.security.jwt.refresh-token-ttl}") Duration refreshTokenTtl,
            @Value("${spring.security.refresh-cookie.secure}") boolean secureCookie,
            @Value("${spring.security.refresh-cookie.same-site}") String sameSite,
            @Value("${spring.security.refresh-cookie.domain}") String cookieDomain) {
        this.authenticationService = authenticationService;
        this.csrfTokenRepository = csrfTokenRepository;
        this.refreshTokenTtl = refreshTokenTtl;
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
        this.cookieDomain = cookieDomain;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a student account")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return sessionResponse(authenticationService.register(request), httpRequest, httpResponse);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate an account")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return sessionResponse(authenticationService.login(request), httpRequest, httpResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the refresh token cookie")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String cookieToken,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        return sessionResponse(authenticationService.refresh(cookieToken), httpRequest, httpResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke the refresh token cookie")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE, required = false) String token,
            HttpServletResponse response) {
        authenticationService.logout(token);
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public UserResponse me(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return UserResponse.from(authenticationService.currentUser(userId));
    }

    private ResponseEntity<AuthResponse> sessionResponse(
            IssuedSession session,
            HttpServletRequest request,
            HttpServletResponse response) {
        setRefreshCookie(response, session.refreshToken());
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
        return ResponseEntity.ok(session.response());
    }

    private void setRefreshCookie(HttpServletResponse response, String value) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/api/v1/auth")
                .maxAge(refreshTokenTtl);
        applyDomain(builder);
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO);
        applyDomain(builder);
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private void applyDomain(ResponseCookie.ResponseCookieBuilder builder) {
        if (!cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
    }
}
