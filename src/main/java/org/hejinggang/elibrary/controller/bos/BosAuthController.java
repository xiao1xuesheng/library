package org.hejinggang.elibrary.controller.bos;

import org.hejinggang.elibrary.common.enums.UserRole;
import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.model.entity.Admin;
import org.hejinggang.elibrary.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理员认证接口
 */
@RestController
@RequestMapping("/bos/auth")
public class BosAuthController {

    private final AdminService adminService;

    public BosAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 管理员登录
     * @param request 登录请求（username, password）
     * @return 管理员信息和Token
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        return ApiResponse.success(adminService.login(
                request.get("username"),
                request.get("password")
        ));
    }

    /**
     * 创建管理员
     * @param request 创建请求（username, password, role）
     * @return 创建的管理员信息
     */
    @PostMapping("/admin")
    public ApiResponse<Map<String, Object>> createAdmin(@RequestBody Map<String, String> request) {
        Long currentAdminId = AuthContext.getCurrentAdminId();
        return ApiResponse.success(adminService.createAdmin(
                request.get("username"),
                request.get("password"),
                UserRole.valueOf(request.get("role")),
                currentAdminId
        ));
    }

    /**
     * 获取所有管理员列表
     * @return 管理员列表
     */
    @GetMapping("/admins")
    public ApiResponse<List<Admin>> listAdmins() {
        return ApiResponse.success(adminService.listAdmins());
    }

    /**
     * 修改管理员角色
     * @param adminId 管理员ID
     * @param request 修改请求（role）
     */
    @PutMapping("/admin/{adminId}/role")
    public ApiResponse<Void> updateAdminRole(@PathVariable Long adminId, @RequestBody Map<String, String> request) {
        Long currentAdminId = AuthContext.getCurrentAdminId();
        adminService.updateAdminRole(adminId, UserRole.valueOf(request.get("role")), currentAdminId);
        return ApiResponse.success(null);
    }

    /**
     * 禁用管理员
     * @param adminId 管理员ID
     */
    @PutMapping("/admin/{adminId}/disable")
    public ApiResponse<Void> disableAdmin(@PathVariable Long adminId) {
        Long currentAdminId = AuthContext.getCurrentAdminId();
        adminService.disableAdmin(adminId, currentAdminId);
        return ApiResponse.success(null);
    }
}