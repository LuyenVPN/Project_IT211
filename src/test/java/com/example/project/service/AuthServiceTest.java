package com.example.project.service;

import com.example.project.model.RefreshToken;
import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import com.example.project.repository.RefreshTokenRepository;
import com.example.project.repository.TokenBlacklistRepository;
import com.example.project.repository.UserRepository;
import com.example.project.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    UserRepository userRepo;
    @Mock
    RefreshTokenRepository refreshTokenRepo;
    @Mock
    JwtUtils jwtUtils;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setUp() {
        // default refresh validity injected via constructor default; nothing to do
    }

    @Test
    void loginSuccess() {
        User u = new User();
        u.setUsername("alice");
        u.setRole(RoleEnum.PATIENT);
        u.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12).encode("secret"));
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(u));
        when(jwtUtils.generateAccessToken(eq("alice"), anyMap())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken("alice")).thenReturn("refresh-token");

        var resp = authService.login("alice", "secret");
        assertNotNull(resp);
        assertEquals("access-token", resp.accessToken());
        assertEquals("refresh-token", resp.refreshToken());
        verify(refreshTokenRepo, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void loginInvalidPassword() {
        User u = new User();
        u.setUsername("bob");
        u.setRole(RoleEnum.PATIENT);
        u.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(12).encode("right"));
        when(userRepo.findByUsername("bob")).thenReturn(Optional.of(u));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login("bob", "wrong"));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }

    @Test
    void refreshRotatesToken() {
        User u = new User();
        u.setUsername("carol");
        u.setRole(RoleEnum.PATIENT);
        RefreshToken stored = new RefreshToken();
        stored.setToken("old-refresh");
        stored.setUser(u);
        stored.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(refreshTokenRepo.findByToken("old-refresh")).thenReturn(Optional.of(stored));
        when(jwtUtils.generateAccessToken(eq("carol"), anyMap())).thenReturn("new-access");
        when(jwtUtils.generateRefreshToken("carol")).thenReturn("new-refresh");

        var resp = authService.refresh("old-refresh");
        assertEquals("new-access", resp.accessToken());
        assertEquals("new-refresh", resp.refreshToken());
        // old token revoked and new saved
        verify(refreshTokenRepo, times(2)).save(any(RefreshToken.class));
    }
}


