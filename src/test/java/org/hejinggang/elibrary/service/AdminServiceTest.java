package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.UserRole;
import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.entity.Admin;
import org.hejinggang.elibrary.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private AdminService adminService;

    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("admin");
        testAdmin.setPasswordHash("admin123");
        testAdmin.setRole(UserRole.ADMIN);
        testAdmin.setStatus(UserStatus.NORMAL);
        testAdmin.setCreatedBy(0L);
    }

    @Test
    void createAdmin_success() {
        when(adminRepository.existsByUsername("newadmin")).thenReturn(false);
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        Map<String, Object> result = adminService.createAdmin("newadmin", "pass123", UserRole.ADMIN, 1L);

        assertNotNull(result);
        assertEquals("admin", result.get("username"));
        assertEquals("ADMIN", result.get("role"));
        verify(operationLogService).logAdminOperation(eq(1L), eq("CREATE_ADMIN"), anyString(), anyLong(), eq("Admin"));
    }

    @Test
    void createAdmin_usernameExists_throwsException() {
        when(adminRepository.existsByUsername("existing")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.createAdmin("existing", "pass123", UserRole.ADMIN, 1L));
        assertEquals(ErrorCode.USERNAME_EXISTS, ex.getCode());
    }

    @Test
    void login_success() {
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));

        Map<String, Object> result = adminService.login("admin", "admin123");

        assertNotNull(result);
        assertEquals("admin", result.get("username"));
        assertNotNull(result.get("token"));
    }

    @Test
    void login_adminNotFound_throwsException() {
        when(adminRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.login("unknown", "pass"));
        assertEquals(ErrorCode.ADMIN_NOT_FOUND, ex.getCode());
    }

    @Test
    void login_wrongPassword_throwsException() {
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.login("admin", "wrongpass"));
        assertEquals(ErrorCode.PASSWORD_INCORRECT, ex.getCode());
    }

    @Test
    void login_disabledAdmin_throwsException() {
        testAdmin.setStatus(UserStatus.DISABLED);
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(testAdmin));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.login("admin", "admin123"));
        assertEquals(ErrorCode.ADMIN_DISABLED, ex.getCode());
    }

    @Test
    void listAdmins_success() {
        when(adminRepository.findAll()).thenReturn(List.of(testAdmin));

        List<Admin> result = adminService.listAdmins();

        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUsername());
    }

    @Test
    void updateAdminRole_success() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        adminService.updateAdminRole(1L, UserRole.ADMIN, 1L);

        assertEquals(UserRole.ADMIN, testAdmin.getRole());
        verify(operationLogService).logAdminOperation(eq(1L), eq("UPDATE_ADMIN_ROLE"), anyString(), eq(1L), eq("Admin"));
    }

    @Test
    void updateAdminRole_notFound_throwsException() {
        when(adminRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.updateAdminRole(999L, UserRole.ADMIN, 1L));
        assertEquals(ErrorCode.ADMIN_NOT_FOUND, ex.getCode());
    }

    @Test
    void disableAdmin_success() {
        when(adminRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(adminRepository.save(any(Admin.class))).thenReturn(testAdmin);

        adminService.disableAdmin(1L, 1L);

        assertEquals(UserStatus.DISABLED, testAdmin.getStatus());
        verify(operationLogService).logAdminOperation(eq(1L), eq("DISABLE_ADMIN"), anyString(), eq(1L), eq("Admin"));
    }

    @Test
    void disableAdmin_notFound_throwsException() {
        when(adminRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.disableAdmin(999L, 1L));
        assertEquals(ErrorCode.ADMIN_NOT_FOUND, ex.getCode());
    }
}