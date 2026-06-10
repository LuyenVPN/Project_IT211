package com.example.project.security;

import com.example.project.model.TokenBlacklist;
import com.example.project.model.User;
import com.example.project.repository.TokenBlacklistRepository;
import com.example.project.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistRepository tokenBlacklistRepo;
    private final UserRepository userRepo;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, TokenBlacklistRepository tokenBlacklistRepo, UserRepository userRepo) {
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistRepo = tokenBlacklistRepo;
        this.userRepo = userRepo;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/login")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/v1/auth/refresh")
                || path.equals("/api/auth/logout")
                || path.equals("/api/v1/auth/logout")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/v1/auth/forgot-password")
                || path.startsWith("/h2-console/")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);
        try {
            // Check blacklist
            Optional<TokenBlacklist> black = tokenBlacklistRepo.findByTokenString(token);
            if (black.isPresent()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revoked");
                return;
            }

            Jws<Claims> jws = jwtUtils.validateAndParse(token);
            Claims claims = jws.getBody();
            String username = claims.getSubject();

            // load user (simple)
            Optional<User> userOpt = userRepo.findByUsername(username);
            if (userOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            }
            User user = userOpt.get();

            // create authorities from role claim or user.role
            String role = claims.get("role", String.class);
            if (role == null) role = user.getRole().name();

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
