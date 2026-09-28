package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.dto.request.NoteRequest;
import org.hejinggang.elibrary.model.entity.Note;
import org.hejinggang.elibrary.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    private Note testNote;

    @BeforeEach
    void setUp() {
        testNote = new Note();
        testNote.setId(1L);
        testNote.setUserId(1L);
        testNote.setBookId(1L);
        testNote.setPage(10);
        testNote.setContent("Test note content");
        testNote.setCreatedAt(LocalDateTime.now());
        testNote.setUpdatedAt(LocalDateTime.now());
        testNote.setIsDeleted(false);
    }

    @Test
    void createNote_success() {
        NoteRequest request = new NoteRequest();
        request.setUserId(1L);
        request.setPage(10);
        request.setContent("Test note");

        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        Note result = noteService.createNote(1L, request);

        assertNotNull(result);
        assertEquals("Test note content", result.getContent());
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void getNotesByBookId_success() {
        when(noteRepository.findByUserIdAndBookIdAndIsDeletedFalse(1L, 1L))
                .thenReturn(List.of(testNote));

        List<Note> result = noteService.getNotesByBookId(1L, 1L);

        assertEquals(1, result.size());
        assertEquals("Test note content", result.get(0).getContent());
    }

    @Test
    void getNotesByBookIdAndPage_success() {
        when(noteRepository.findByUserIdAndBookIdAndPageAndIsDeletedFalse(1L, 1L, 10))
                .thenReturn(List.of(testNote));

        List<Note> result = noteService.getNotesByBookIdAndPage(1L, 1L, 10);

        assertEquals(1, result.size());
    }

    @Test
    void getAllNotesByUserId_success() {
        when(noteRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testNote));

        List<Note> result = noteService.getAllNotesByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void updateNote_success() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        Note result = noteService.updateNote(1L, 1L, "Updated content");

        assertEquals("Updated content", testNote.getContent());
        assertNotNull(result);
    }

    @Test
    void updateNote_notFound_throwsException() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> noteService.updateNote(999L, 1L, "Updated"));
        assertEquals(ErrorCode.NOTE_NOT_FOUND, ex.getCode());
    }

    @Test
    void updateNote_notOwner_throwsException() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> noteService.updateNote(1L, 2L, "Updated"));
        assertEquals(ErrorCode.NOT_YOUR_NOTE, ex.getCode());
    }

    @Test
    void updateNote_deleted_throwsException() {
        testNote.setIsDeleted(true);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> noteService.updateNote(1L, 1L, "Updated"));
        assertEquals(ErrorCode.NOTE_NOT_FOUND, ex.getCode());
    }

    @Test
    void deleteNote_success() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        noteService.deleteNote(1L, 1L);

        assertTrue(testNote.getIsDeleted());
        assertNotNull(testNote.getDeletedAt());
    }

    @Test
    void deleteNote_notFound_throwsException() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> noteService.deleteNote(999L, 1L));
        assertEquals(ErrorCode.NOTE_NOT_FOUND, ex.getCode());
    }

    @Test
    void deleteNote_notOwner_throwsException() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> noteService.deleteNote(1L, 2L));
        assertEquals(ErrorCode.NOT_YOUR_NOTE, ex.getCode());
    }
}