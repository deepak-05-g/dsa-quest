package com.dsaquest.auth.service;

import com.dsaquest.auth.dto.AuthResponse;
import com.dsaquest.auth.dto.LoginRequest;
import com.dsaquest.auth.dto.RegisterRequest;
import com.dsaquest.auth.dto.UserResponse;
import com.dsaquest.auth.entity.RefreshToken;
import com.dsaquest.auth.entity.Role;
import com.dsaquest.auth.entity.User;
import com.dsaquest.auth.repository.RefreshTokenRepository;
import com.dsaquest.auth.repository.UserRepository;
import com.dsaquest.auth.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final TokenHashService tokenHashService;
    private final JwtService jwtService;
    private final Duration refreshTokenTtl;

    public AuthenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy,
            TokenHashService tokenHashService,
            JwtService jwtService,
            @Value("${spring.security.jwt.refresh-token-ttl}") Duration refreshTokenTtl) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.tokenHashService = tokenHashService;
        this.jwtService = jwtService;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    @Transactional
    public IssuedSession register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (!request.password().equals(request.passwordConfirmation())) {
            throw new AuthenticationValidationException("Password confirmation does not match");
        }
        if (!passwordPolicy.isValid(request.password())) {
            throw new AuthenticationValidationException("Password does not meet the security policy");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }
        User user;
        try {
            user = userRepository.save(new User(
                    UUID.randomUUID(),
                    request.fullName().trim(),
                    email,
                    passwordEncoder.encode(request.password()),
                    Role.STUDENT));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
        return issueSession(user, UUID.randomUUID());
    }

    @Transactional
    public IssuedSession login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email())).orElse(null);
        if (user == null || !user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return issueSession(user, UUID.randomUUID());
    }

    @Transactional
    public IssuedSession refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        RefreshToken current = refreshTokenRepository.findByTokenHash(tokenHashService.hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (current.getRevokedAt() != null) {
            refreshTokenRepository.revokeFamily(current.getFamilyId(), Instant.now());
            throw new InvalidRefreshTokenException();
        }
        if (current.getExpiresAt().isBefore(Instant.now()) || !current.getUser().isEnabled()) {
            current.revoke();
            throw new InvalidRefreshTokenException();
        }
        current.revoke();
        return issueSession(current.getUser(), current.getFamilyId());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(tokenHashService.hash(rawRefreshToken))
                .ifPresent(RefreshToken::revoke);
    }

    @Transactional(readOnly = true)
    public User currentUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isEnabled)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private IssuedSession issueSession(User user, UUID familyId) {
        String rawRefreshToken = generateRefreshToken();
        Instant expiresAt = Instant.now().plus(refreshTokenTtl);
        refreshTokenRepository.save(new RefreshToken(
                UUID.randomUUID(),
                user,
                tokenHashService.hash(rawRefreshToken),
                familyId,
                expiresAt));
        JwtService.Token accessToken = jwtService.createAccessToken(user);
        return new IssuedSession(
                new AuthResponse(accessToken.value(), "Bearer", accessToken.expiresAt(), UserResponse.from(user)),
                rawRefreshToken);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[64];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public record IssuedSession(AuthResponse response, String refreshToken) {
    }

    public static class AuthenticationValidationException extends RuntimeException {
        public AuthenticationValidationException(String message) { super(message); }
    }

    public static class DuplicateEmailException extends RuntimeException {
    }

    public static class InvalidCredentialsException extends RuntimeException {
    }

    public static class InvalidRefreshTokenException extends RuntimeException {
    }
}
