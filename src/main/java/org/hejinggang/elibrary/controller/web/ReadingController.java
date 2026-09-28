package org.hejinggang.elibrary.controller.web;

import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.service.ReadingHistoryResponse;
import org.hejinggang.elibrary.service.ReadingProgressResponse;
import org.hejinggang.elibrary.service.ReadingService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端阅读接口
 */
@RestController
@RequestMapping("/api")
public class ReadingController {

    private final ReadingService readingService;

    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    /**
     * 保存阅读进度
     * @param bookId 书籍ID
     * @param page 当前页码
     * @param chapter 当前章节（可选）
     * @param scrollPosition 滚动位置（可选）
     * @param readDuration 本次阅读时长（秒，可选）
     * @param deviceType 设备类型（可选）
     * @return 阅读进度信息
     */
    @PostMapping("/reading/progress")
    public ApiResponse<ReadingProgressResponse> saveReadingProgress(
            @RequestParam Long bookId,
            @RequestParam Integer page,
            @RequestParam(required = false) String chapter,
            @RequestParam(required = false) Integer scrollPosition,
            @RequestParam(required = false) Integer readDuration,
            @RequestParam(required = false) String deviceType) {
        return ApiResponse.success(readingService.saveReadingProgress(
                AuthContext.getCurrentUserId(), bookId, page, chapter, scrollPosition, readDuration, deviceType));
    }

    /**
     * 获取阅读进度
     * @param bookId 书籍ID
     * @return 阅读进度信息
     */
    @GetMapping("/reading/progress/{bookId}")
    public ApiResponse<ReadingProgressResponse> getReadingProgress(@PathVariable Long bookId) {
        return ApiResponse.success(readingService.getReadingProgress(AuthContext.getCurrentUserId(), bookId));
    }

    /**
     * 获取阅读历史列表
     * @param page 页码
     * @param size 每页数量
     * @return 阅读历史分页列表
     */
    @GetMapping("/reading/history")
    public ApiResponse<Page<ReadingHistoryResponse>> getReadingHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(readingService.getReadingHistory(AuthContext.getCurrentUserId(), page, size));
    }
}