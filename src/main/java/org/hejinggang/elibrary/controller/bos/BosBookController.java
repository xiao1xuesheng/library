package org.hejinggang.elibrary.controller.bos;

import jakarta.validation.Valid;
import org.hejinggang.elibrary.common.enums.BookStatus;
import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.model.dto.request.BookCreateRequest;
import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理书籍接口
 */
@RestController
@RequestMapping("/bos/books")
public class BosBookController {

    private final BookService bookService;

    public BosBookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 上架新书
     * @param request 书籍创建请求
     * @return 书籍信息
     */
    @PostMapping
    public ApiResponse<Book> createBook(@Valid @RequestBody BookCreateRequest request) {
        return ApiResponse.success(bookService.createBook(request));
    }

    /**
     * 更新书籍信息
     * @param id 书籍ID
     * @param request 书籍更新请求
     * @return 更新后的书籍信息
     */
    @PutMapping("/{id}")
    public ApiResponse<Book> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookCreateRequest request) {
        return ApiResponse.success(bookService.updateBook(id, request));
    }

    /**
     * 更新书籍状态（上架/下架）
     * @param id 书籍ID
     * @param status 目标状态
     * @return 更新后的书籍信息
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Book> updateBookStatus(
            @PathVariable Long id,
            @RequestParam BookStatus status) {
        return ApiResponse.success(bookService.updateBookStatus(id, status));
    }

    /**
     * 分页查询所有书籍（含下架）
     * @param status 书籍状态（可选）
     * @param category 分类（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 书籍分页列表
     */
    @GetMapping
    public ApiResponse<Page<Book>> listBooks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(bookService.listAllBooksForBos(status, category, page, size));
    }
}