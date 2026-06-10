package com.example.project.service;

import com.example.project.model.RoleEnum;
import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void createUserSuccess() {
        when(userRepository.findByUsername("doctor2")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Doctor123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });

        var request = new UserService.UserRequest("doctor2", "Doctor123", RoleEnum.DOCTOR, true);
        var response = userService.create(request);

        assertEquals(7L, response.id());
        assertEquals(RoleEnum.DOCTOR, response.role());
    }

    @Test
    void searchByKeywordUsesRepositoryPaging() {
        User user = new User();
        user.setId(1L);
        user.setUsername("patient1");
        user.setRole(RoleEnum.PATIENT);
        user.setIsActive(true);
        var pageable = PageRequest.of(0, 10);
        when(userRepository.findByUsernameContainingIgnoreCase("patient", pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        var page = userService.search("patient", pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("patient1", page.getContent().get(0).username());
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        User user = new User();
        user.setUsername("patient1");
        user.setPasswordHash("hash");
        when(userRepository.findByUsername("patient1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("patient1", "wrong", "newpass"));
    }

    @Test
    void resetPasswordUpdatesHash() {
        User user = new User();
        user.setUsername("patient1");
        when(userRepository.findByUsername("patient1")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("new-hash");

        userService.resetPassword("patient1", "newpass");

        assertEquals("new-hash", user.getPasswordHash());
        verify(userRepository).save(user);
    }
}
