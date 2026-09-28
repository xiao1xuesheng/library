package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.dto.request.NoteRequest;
import org.hejinggang.elibrary.model.entity.Note;
import org.hejinggang.elibrary.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Transactional
    public Note createNote(Long bookId, NoteRequest request) {
        Note note = new Note();
        note.setUserId(request.getUserId());
        note.setBookId(bookId);
        note.setPage(request.getPage());
        note.setContent(request.getContent());
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    public List<Note> getNotesByBookId(Long bookId, Long userId) {
        return noteRepository.findByUserIdAndBookIdAndIsDeletedFalse(userId, bookId);
    }

    public List<Note> getNotesByBookIdAndPage(Long bookId, Long userId, Integer page) {
        return noteRepository.findByUserIdAndBookIdAndPageAndIsDeletedFalse(userId, bookId, page);
    }

    public List<Note> getAllNotesByUserId(Long userId) {
        return noteRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Note updateNote(Long noteId, Long userId, String content) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND, "Note not found"));

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_NOTE, "Not your note");
        }

        if (note.getIsDeleted()) {
            throw new BusinessException(ErrorCode.NOTE_NOT_FOUND, "Note has been deleted");
        }

        note.setContent(content);
        note.setUpdatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND, "Note not found"));

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_NOTE, "Not your note");
        }

        note.setIsDeleted(true);
        note.setDeletedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteRepository.save(note);
    }
}