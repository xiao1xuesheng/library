package org.hejinggang.elibrary.controller.bos;

import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.model.dto.response.ReviewResponse;
import org.hejinggang.elibrary.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理评论接口
 */
@RestController
@RequestMapping("/bos/reviews")
public class BosReviewController {

    private final ReviewService reviewService;

    public BosReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 分页查询评论列表（可指定书籍）
     * @param bookId 书籍ID（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 评论分页列表
     */
    @GetMapping
    public ApiResponse<Page<ReviewResponse>> listReviews(
            @RequestParam(required = false) Long bookId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(reviewService.getAllReviewsForBos(bookId, page, size));
    }

    /**
     * 管理员删除评论（软删除，记录删除原因）
     * @param id 评论ID
     * @param reason 删除原因
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReview(
            @PathVariable Long id,
            @RequestParam String reason) {
        Long adminId = AuthContext.getCurrentAdminId();
        reviewService.deleteReviewByAdmin(id, adminId, reason);
        return ApiResponse.success(null);
    }
}