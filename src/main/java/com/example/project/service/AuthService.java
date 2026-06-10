package com.example.project.service;

import com.example.project.model.RefreshToken;
import com.example.project.model.TokenBlacklist;
import com.example.project.model.User;
import com.example.project.repository.RefreshTokenRepository;
import com.example.project.repository.TokenBlacklistRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final TokenBlacklistRepository tokenBlacklistRepo;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final long refreshTokenValiditySeconds;

    @Autowired
    public AuthService(UserRepository userRepo,
                       RefreshTokenRepository refreshTokenRepo,
                       TokenBlacklistRepository tokenBlacklistRepo,
                       JwtUtils jwtUtils,
                       @Value("${app.jwt.refresh-expiration-seconds:604800}") long refreshTokenValiditySeconds) {
        this.userRepo = userRepo;
        this.refreshTokenRepo = refreshTokenRepo;
        this.tokenBlacklistRepo = tokenBlacklistRepo;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = new BCryptPasswordEncoder(12); // strength >= 10 per SRS
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    @Transactional
    public AuthResponse login(String username, String password) {
        Optional<User> opt = userRepo.findByUsername(username);
        if (opt.isEmpty()) throw new RuntimeException("Invalid credentials");

        User user = opt.get();
        if (!verifyPassword(user, password)) throw new RuntimeException("Invalid credentials");

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        String accessToken = jwtUtils.generateAccessToken(user.getUsername(), claims);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        // Save refresh token
        RefreshToken rt = new RefreshToken();
        rt.setToken(refreshToken);
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds));
        rt.setUser(user);
        refreshTokenRepo.save(rt);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public User registerPatient(String username, String rawPassword) {
        if (userRepo.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(com.example.project.model.RoleEnum.PATIENT);
        user.setIsActive(true);
        return userRepo.save(user);
    }

    @Transactional
    public AuthResponse refresh(String oldRefreshToken) {
        RefreshToken stored = refreshTokenRepo.findByToken(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (stored.getRevokedAt() != null) {
            throw new RuntimeException("Refresh token revoked");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        // Rotate: revoke old
        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenRepo.save(stored);

        User user = stored.getUser();
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        String newAccess = jwtUtils.generateAccessToken(user.getUsername(), claims);
        String newRefresh = jwtUtils.generateRefreshToken(user.getUsername());

        RefreshToken newRt = new RefreshToken();
        newRt.setToken(newRefresh);
        newRt.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds));
        newRt.setUser(user);
        refreshTokenRepo.save(newRt);

        return new AuthResponse(newAccess, newRefresh);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        User user = refreshTokenRepo.findByToken(refreshToken)
                .map(RefreshToken::getUser)
                .orElseGet(() -> {
                    String username = jwtUtils.validateAndParse(accessToken).getBody().getSubject();
                    return userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
                });

        // blacklist the access token
        TokenBlacklist tb = new TokenBlacklist();
        tb.setTokenString(accessToken);
        tb.setRevokedAt(LocalDateTime.now());
        tb.setUser(user);
        tokenBlacklistRepo.save(tb);

        // revoke refresh token if present
        refreshTokenRepo.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevokedAt(LocalDateTime.now());
            refreshTokenRepo.save(rt);
        });
    }

    public static record AuthResponse(String accessToken, String refreshToken) {}
}
