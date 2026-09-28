package org.hejinggang.elibrary.controller.web;

import jakarta.validation.Valid;
import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.config.AuthContext;
import org.hejinggang.elibrary.model.dto.request.NoteRequest;
import org.hejinggang.elibrary.model.entity.Note;
import org.hejinggang.elibrary.service.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端备注接口
 */
@RestController
@RequestMapping("/api")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * 创建备注
     * @param bookId 书籍ID
     * @param request 备注请求（content, page）
     * @return 备注信息
     */
    @PostMapping("/books/{bookId}/notes")
    public ApiResponse<Note> createNote(
            @PathVariable Long bookId,
            @Valid @RequestBody NoteRequest request) {
        request.setUserId(AuthContext.getCurrentUserId());
        return ApiResponse.success(noteService.createNote(bookId, request));
    }

    /**
     * 查看书籍备注（仅查看自己的）
     * @param bookId 书籍ID
     * @param page 页码（可选）
     * @return 备注列表
     */
    @GetMapping("/books/{bookId}/notes")
    public ApiResponse<List<Note>> getNotes(
            @PathVariable Long bookId,
            @RequestParam(required = false) Integer page) {
        Long userId = AuthContext.getCurrentUserId();
        if (page != null) {
            return ApiResponse.success(noteService.getNotesByBookIdAndPage(bookId, userId, page));
        }
        return ApiResponse.success(noteService.getNotesByBookId(bookId, userId));
    }

    /**
     * 查看我的所有备注
     * @return 备注列表
     */
    @GetMapping("/notes")
    public ApiResponse<List<Note>> getAllMyNotes() {
        return ApiResponse.success(noteService.getAllNotesByUserId(AuthContext.getCurrentUserId()));
    }

    /**
     * 编辑自己的备注
     * @param id 备注ID
     * @param request 备注更新请求（content）
     * @return 更新后的备注
     */
    @PutMapping("/notes/{id}")
    public ApiResponse<Note> updateNote(
            @PathVariable Long id,
            @RequestBody NoteUpdateRequest request) {
        return ApiResponse.success(noteService.updateNote(id, AuthContext.getCurrentUserId(), request.getContent()));
    }

    /**
     * 删除自己的备注（软删除）
     * @param id 备注ID
     * @return 无内容
     */
    @DeleteMapping("/notes/{id}")
    public ApiResponse<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id, AuthContext.getCurrentUserId());
        return ApiResponse.success(null);
    }

    public static class NoteUpdateRequest {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}