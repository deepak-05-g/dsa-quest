package com.dsaquest.auth.service;

import com.dsaquest.auth.dto.RegisterRequest;
import com.dsaquest.auth.entity.RefreshToken;
import com.dsaquest.auth.entity.Role;
import com.dsaquest.auth.entity.User;
import com.dsaquest.auth.repository.RefreshTokenRepository;
import com.dsaquest.auth.repository.UserRepository;
import com.dsaquest.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                new PasswordPolicy(),
                new TokenHashService(),
                jwtService,
                Duration.ofDays(30));
    }

    @Test
    void registersStudentWithNormalizedEmailAndHashedPassword() {
        RegisterRequest request = new RegisterRequest(
                "  Ada Lovelace ",
                " ADA@example.com ",
                "StrongPassword123",
                "StrongPassword123");
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createAccessToken(any(User.class)))
                .thenReturn(new JwtService.Token("access-token", Instant.now().plusSeconds(900)));

        AuthenticationService.IssuedSession session = service.register(request);

        assertThat(session.response().user().email()).isEqualTo("ada@example.com");
        assertThat(session.response().user().role()).isEqualTo(Role.STUDENT);
        assertThat(session.refreshToken()).isNotBlank();
        verify(passwordEncoder).encode(request.password());
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void rejectsDuplicateEmailBeforeHashingPassword() {
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.register(new RegisterRequest(
                        "Ada Lovelace",
                        "ada@example.com",
                        "StrongPassword123",
                        "StrongPassword123")))
                .isInstanceOf(AuthenticationService.DuplicateEmailException.class);

        verifyNoInteractions(passwordEncoder, refreshTokenRepository, jwtService);
    }

    @Test
    void revokesRefreshTokenFamilyWhenAReusedTokenIsPresented() {
        User user = new User(
                UUID.randomUUID(),
                "Ada Lovelace",
                "ada@example.com",
                "hashed-password",
                Role.STUDENT);
        UUID familyId = UUID.randomUUID();
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                user,
                new TokenHashService().hash("old-refresh-token"),
                familyId,
                Instant.now().plus(Duration.ofDays(1)));
        token.revoke();
        when(refreshTokenRepository.findByTokenHash(new TokenHashService().hash("old-refresh-token")))
                .thenReturn(java.util.Optional.of(token));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.refresh("old-refresh-token"))
                .isInstanceOf(AuthenticationService.InvalidRefreshTokenException.class);

        verify(refreshTokenRepository).revokeFamily(eq(familyId), any(Instant.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    void rejectsExpiredRefreshToken() {
        User user = new User(
                UUID.randomUUID(),
                "Ada Lovelace",
                "ada@example.com",
                "hashed-password",
                Role.STUDENT);
        RefreshToken token = new RefreshToken(
                UUID.randomUUID(),
                user,
                new TokenHashService().hash("expired-refresh-token"),
                UUID.randomUUID(),
                Instant.now().minus(Duration.ofMinutes(1)));
        when(refreshTokenRepository.findByTokenHash(new TokenHashService().hash("expired-refresh-token")))
                .thenReturn(java.util.Optional.of(token));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.refresh("expired-refresh-token"))
                .isInstanceOf(AuthenticationService.InvalidRefreshTokenException.class);

        assertThat(token.getRevokedAt()).isNotNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void rejectsDisabledUserAtLogin() {
        User user = new User(
                UUID.randomUUID(),
                "Ada Lovelace",
                "ada@example.com",
                "hashed-password",
                Role.STUDENT);
        ReflectionTestUtils.setField(user, "enabled", false);
        when(userRepository.findByEmail("ada@example.com")).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.matches("StrongPassword123", "hashed-password")).thenReturn(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.login(
                        new com.dsaquest.auth.dto.LoginRequest("ada@example.com", "StrongPassword123")))
                .isInstanceOf(AuthenticationService.InvalidCredentialsException.class);

        verifyNoInteractions(refreshTokenRepository, jwtService);
    }
}
