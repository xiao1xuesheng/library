package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.UserRole;
import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.entity.Admin;
import org.hejinggang.elibrary.repository.AdminRepository;
import org.hejinggang.elibrary.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final OperationLogService operationLogService;

    public AdminService(AdminRepository adminRepository, OperationLogService operationLogService) {
        this.adminRepository = adminRepository;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public Map<String, Object> createAdmin(String username, String password, UserRole role, Long createdBy) {
        if (adminRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "管理员用户名已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPasswordHash(hashPassword(password));
        admin.setRole(role);
        admin.setStatus(UserStatus.NORMAL);
        admin.setCreatedBy(createdBy);
        admin = adminRepository.save(admin);

        operationLogService.logAdminOperation(createdBy, "CREATE_ADMIN", 
                "创建管理员: " + username, admin.getId(), "Admin");

        Map<String, Object> result = new HashMap<>();
        result.put("adminId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("role", admin.getRole().name());
        return result;
    }

    public Map<String, Object> login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND, "管理员不存在"));

        if (!admin.getPasswordHash().equals(hashPassword(password))) {
            throw new BusinessException(ErrorCode.PASSWORD_INCORRECT, "密码错误");
        }

        if (admin.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.ADMIN_DISABLED, "管理员已被禁用");
        }

        String token = JwtUtil.generateAdminToken(admin.getId(), admin.getUsername(), admin.getRole().name());

        Map<String, Object> result = new HashMap<>();
        result.put("adminId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("role", admin.getRole().name());
        result.put("token", token);
        return result;
    }

    public List<Admin> listAdmins() {
        return adminRepository.findAll();
    }

    @Transactional
    public void updateAdminRole(Long adminId, UserRole newRole, Long operatorId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND, "管理员不存在"));

        UserRole oldRole = admin.getRole();
        admin.setRole(newRole);
        adminRepository.save(admin);

        operationLogService.logAdminOperation(operatorId, "UPDATE_ADMIN_ROLE",
                "修改管理员角色: " + oldRole + " -> " + newRole, adminId, "Admin");
    }

    @Transactional
    public void disableAdmin(Long adminId, Long operatorId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND, "管理员不存在"));

        admin.setStatus(UserStatus.DISABLED);
        adminRepository.save(admin);

        operationLogService.logAdminOperation(operatorId, "DISABLE_ADMIN",
                "禁用管理员", adminId, "Admin");
    }

    private String hashPassword(String password) {
        return password;
    }
}