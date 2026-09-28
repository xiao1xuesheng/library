package org.hejinggang.elibrary.controller.bos;

import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.model.dto.response.BorrowRecordResponse;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.service.BorrowService;
import org.hejinggang.elibrary.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理用户接口
 */
@RestController
@RequestMapping("/bos")
public class BosUserController {

    private final UserService userService;
    private final BorrowService borrowService;

    public BosUserController(UserService userService, BorrowService borrowService) {
        this.userService = userService;
        this.borrowService = borrowService;
    }

    /**
     * 分页查询用户列表
     * @param userType 用户类型（可选）
     * @param userStatus 用户状态（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 用户分页列表
     */
    @GetMapping("/users")
    public ApiResponse<Page<User>> listUsers(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String userStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(userService.listUsers(userType, userStatus, page, size));
    }

    /**
     * 查看用户借阅记录
     * @param id 用户ID
     * @return 借阅记录列表
     */
    @GetMapping("/users/{id}/borrows")
    public ApiResponse<List<BorrowRecordResponse>> getUserBorrows(@PathVariable Long id) {
        return ApiResponse.success(borrowService.getUserBorrowsForBos(id));
    }

    /**
     * 修改用户类型（普通用户/VIP）
     * @param id 用户ID
     * @param userType 目标用户类型
     * @return 更新后的用户信息
     */
    @PutMapping("/users/{id}/type")
    public ApiResponse<User> changeUserType(
            @PathVariable Long id,
            @RequestParam UserType userType) {
        return ApiResponse.success(userService.changeUserType(id, userType));
    }

    /**
     * 修改用户状态（正常/禁言/禁用）
     * @param id 用户ID
     * @param userStatus 目标用户状态
     * @return 更新后的用户信息
     */
    @PutMapping("/users/{id}/status")
    public ApiResponse<User> changeUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus userStatus) {
        return ApiResponse.success(userService.changeUserStatus(id, userStatus));
    }
}