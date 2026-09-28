package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserIdAndBookIdAndIsDeletedFalse(Long userId, Long bookId);

    List<Note> findByUserIdAndBookIdAndPageAndIsDeletedFalse(Long userId, Long bookId, Integer page);

    List<Note> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);
}