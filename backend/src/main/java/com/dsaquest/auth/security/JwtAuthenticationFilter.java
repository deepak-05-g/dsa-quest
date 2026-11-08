package com.dsaquest.auth.security;

import com.dsaquest.auth.entity.User;
import com.dsaquest.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authenticate(authorization.substring(7));
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        try {
            Claims claims = jwtService.parse(token);
            UUID userId = UUID.fromString(claims.getSubject());
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.isEnabled()) {
                var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                var authentication = new UsernamePasswordAuthenticationToken(
                        user.getId().toString(), null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
    }
}
