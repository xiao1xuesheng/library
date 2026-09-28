package org.hejinggang.elibrary.controller.web;

import jakarta.validation.Valid;
import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.model.dto.request.BorrowRequest;
import org.hejinggang.elibrary.model.dto.response.BorrowRecordResponse;
import org.hejinggang.elibrary.service.BorrowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端借阅接口
 */
@RestController
@RequestMapping("/api")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * 借阅书籍
     * @param request 借阅请求（bookId, requestId）
     * @return 借阅记录
     */
    @PostMapping("/borrow")
    public ApiResponse<BorrowRecordResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        request.setUserId(AuthContext.getCurrentUserId());
        return ApiResponse.success(borrowService.borrowBook(request));
    }

    /**
     * 归还书籍
     * @param id 借阅记录ID
     * @return 借阅记录
     */
    @PostMapping("/borrow/{id}/return")
    public ApiResponse<BorrowRecordResponse> returnBook(@PathVariable Long id) {
        return ApiResponse.success(borrowService.returnBook(id, AuthContext.getCurrentUserId()));
    }

    /**
     * 借阅续期
     * @param id 借阅记录ID
     * @param requestId 幂等请求ID
     * @return 借阅记录
     */
    @PostMapping("/borrow/{id}/renew")
    public ApiResponse<BorrowRecordResponse> renewBorrow(
            @PathVariable Long id,
            @RequestParam(required = false) String requestId) {
        return ApiResponse.success(borrowService.renewBorrow(id, AuthContext.getCurrentUserId(), requestId));
    }

    /**
     * 查看我的借阅记录
     * @param status 借阅状态（可选）
     * @return 借阅记录列表
     */
    @GetMapping("/my/borrows")
    public ApiResponse<List<BorrowRecordResponse>> getMyBorrows(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(borrowService.getMyBorrows(AuthContext.getCurrentUserId(), status));
    }
}