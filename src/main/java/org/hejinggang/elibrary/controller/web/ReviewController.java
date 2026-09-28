package org.hejinggang.elibrary.controller.web;

import jakarta.validation.Valid;
import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.model.dto.request.ReviewRequest;
import org.hejinggang.elibrary.model.dto.response.ReviewResponse;
import org.hejinggang.elibrary.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端评论接口
 */
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 发表评论
     * @param bookId 书籍ID
     * @param request 评论请求（content, pageReference, rating, requestId）
     * @return 评论信息
     */
    @PostMapping("/books/{bookId}/reviews")
    public ApiResponse<ReviewResponse> createReview(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewRequest request) {
        request.setUserId(AuthContext.getCurrentUserId());
        return ApiResponse.success(reviewService.createReview(bookId, request));
    }

    /**
     * 查看书籍评论列表
     * @param bookId 书籍ID
     * @param page 页码
     * @param size 每页数量
     * @return 评论分页列表
     */
    @GetMapping("/books/{bookId}/reviews")
    public ApiResponse<Page<ReviewResponse>> getReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(reviewService.getReviewsByBookId(bookId, page, size));
    }

    /**
     * 编辑自己的评论
     * @param id 评论ID
     * @param request 评论更新请求（content）
     * @return 更新后的评论
     */
    @PutMapping("/reviews/{id}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long id,
            @RequestBody ReviewUpdateRequest request) {
        return ApiResponse.success(reviewService.updateReview(id, AuthContext.getCurrentUserId(), request.getContent()));
    }

    /**
     * 删除自己的评论（软删除）
     * @param id 评论ID
     * @return 无内容
     */
    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id, AuthContext.getCurrentUserId());
        return ApiResponse.success(null);
    }

    public static class ReviewUpdateRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}