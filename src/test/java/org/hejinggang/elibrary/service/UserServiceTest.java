package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.entity.Account;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.AccountRepository;
import org.hejinggang.elibrary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("123456");
        testUser.setUserType(UserType.NORMAL);
        testUser.setUserStatus(UserStatus.NORMAL);
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(accountRepository.save(any(Account.class))).thenReturn(new Account());

        Map<String, Object> result = userService.register("newuser", "new@example.com", "password");

        assertNotNull(result);
        assertEquals("testuser", result.get("username"));
        assertNotNull(result.get("token"));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void register_usernameExists_throwsException() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.register("existing", "email@test.com", "pass"));
        assertEquals(ErrorCode.USERNAME_EXISTS, ex.getCode());
    }

    @Test
    void login_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Map<String, Object> result = userService.login("testuser", "123456");

        assertNotNull(result);
        assertEquals("testuser", result.get("username"));
        assertNotNull(result.get("token"));
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login("unknown", "pass"));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getCode());
    }

    @Test
    void login_wrongPassword_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login("testuser", "wrongpass"));
        assertEquals(ErrorCode.PASSWORD_INCORRECT, ex.getCode());
    }

    @Test
    void login_disabledUser_throwsException() {
        testUser.setUserStatus(UserStatus.DISABLED);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login("testuser", "123456"));
        assertEquals(ErrorCode.USER_DISABLED, ex.getCode());
    }

    @Test
    void getUserProfile_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserProfile(1L);

        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserProfile_notFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.getUserProfile(999L));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getCode());
    }

    @Test
    void listUsers_noFilters() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.listUsers(null, null, 1, 10);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void listUsers_filterByType() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findByUserType(eq(UserType.NORMAL), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.listUsers("NORMAL", null, 1, 10);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByUserType(eq(UserType.NORMAL), any(Pageable.class));
    }

    @Test
    void listUsers_filterByStatus() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findByUserStatus(eq(UserStatus.NORMAL), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.listUsers(null, "NORMAL", 1, 10);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByUserStatus(eq(UserStatus.NORMAL), any(Pageable.class));
    }

    @Test
    void listUsers_filterByTypeAndStatus() {
        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findByUserTypeAndUserStatus(eq(UserType.NORMAL), eq(UserStatus.NORMAL), any(Pageable.class))).thenReturn(page);

        Page<User> result = userService.listUsers("NORMAL", "NORMAL", 1, 10);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByUserTypeAndUserStatus(eq(UserType.NORMAL), eq(UserStatus.NORMAL), any(Pageable.class));
    }

    @Test
    void changeUserType_toVip() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.changeUserType(1L, UserType.VIP);

        assertEquals(UserType.VIP, result.getUserType());
        assertNotNull(result.getVipExpireAt());
    }

    @Test
    void changeUserType_toNormal() {
        testUser.setUserType(UserType.VIP);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.changeUserType(1L, UserType.NORMAL);

        assertEquals(UserType.NORMAL, result.getUserType());
    }

    @Test
    void changeUserType_userNotFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.changeUserType(999L, UserType.VIP));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getCode());
    }

    @Test
    void changeUserStatus_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.changeUserStatus(1L, UserStatus.DISABLED);

        assertEquals(UserStatus.DISABLED, result.getUserStatus());
    }

    @Test
    void changeUserStatus_userNotFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.changeUserStatus(999L, UserStatus.DISABLED));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getCode());
    }
}