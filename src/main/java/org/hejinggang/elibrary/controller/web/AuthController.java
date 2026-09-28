package org.hejinggang.elibrary.controller.web;

import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户端认证接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     * @param request 注册请求（username, email, password）
     * @return 用户信息和Token
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        return ApiResponse.success(userService.register(
                request.get("username"),
                request.get("email"),
                request.get("password")
        ));
    }

    /**
     * 用户登录
     * @param request 登录请求（username, password）
     * @return 用户信息和Token
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        return ApiResponse.success(userService.login(
                request.get("username"),
                request.get("password")
        ));
    }

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @GetMapping("/profile")
    public ApiResponse<?> getProfile() {
        Long userId = org.hejinggang.elibrary.config.AuthContext.getCurrentUserId();
        return ApiResponse.success(userService.getUserProfile(userId));
    }
}