package org.hejinggang.elibrary.controller.web;

import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.model.dto.response.BookDetailResponse;
import org.hejinggang.elibrary.model.dto.response.BookListResponse;
import org.hejinggang.elibrary.service.BookService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端书籍接口
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 分页浏览书籍
     * @param keyword 书名关键词
     * @param author 作者
     * @param category 分类
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @param page 页码
     * @param size 每页数量
     * @return 书籍列表
     */
    @GetMapping
    public ApiResponse<BookListResponse> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(bookService.listBooks(keyword, author, category, sortBy, sortOrder, page, size));
    }

    /**
     * 获取书籍详情
     * @param id 书籍ID
     * @return 书籍详情
     */
    @GetMapping("/{id}")
    public ApiResponse<BookDetailResponse> getBookDetail(@PathVariable Long id) {
        Long userId = null;
        try {
            userId = AuthContext.getCurrentUserId();
        } catch (SecurityException e) {
            // 未登录用户也可以查看书籍详情
        }
        return ApiResponse.success(bookService.getBookDetail(id, userId));
    }
}