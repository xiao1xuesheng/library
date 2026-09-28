package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.model.entity.BorrowRecord;
import org.hejinggang.elibrary.model.entity.ReadingHistory;
import org.hejinggang.elibrary.repository.BookRepository;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.hejinggang.elibrary.repository.ReadingHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    @Mock
    private ReadingHistoryRepository readingHistoryRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReadingService readingService;

    private Book testBook;
    private ReadingHistory testHistory;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setCoverUrl("https://example.com/cover.jpg");
        testBook.setPageCount(300);

        testHistory = new ReadingHistory();
        testHistory.setId(1L);
        testHistory.setUserId(1L);
        testHistory.setBookId(1L);
        testHistory.setPage(50);
        testHistory.setChapter("Chapter 3");
        testHistory.setScrollPosition(200);
        testHistory.setReadDuration(120);
        testHistory.setDeviceType("web");
        testHistory.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void saveReadingProgress_success() {
        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setStatus(BorrowStatus.ACTIVE);
        record.setDueDate(LocalDateTime.now().plusDays(7));

        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.of(record));
        when(readingHistoryRepository.save(any(ReadingHistory.class))).thenReturn(testHistory);

        ReadingProgressResponse result = readingService.saveReadingProgress(1L, 1L, 50, "Chapter 3", 200, 120, "web");

        assertNotNull(result);
        assertEquals(50, result.getPage());
        assertEquals("Chapter 3", result.getChapter());
    }

    @Test
    void saveReadingProgress_noBorrow_throwsException() {
        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> readingService.saveReadingProgress(1L, 1L, 50, "Chapter 3", 200, 120, "web"));
        assertEquals(ErrorCode.NO_READ_PERMISSION, ex.getCode());
    }

    @Test
    void saveReadingProgress_nullDuration() {
        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setStatus(BorrowStatus.ACTIVE);
        record.setDueDate(LocalDateTime.now().plusDays(7));

        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.of(record));
        when(readingHistoryRepository.save(any(ReadingHistory.class))).thenReturn(testHistory);

        ReadingProgressResponse result = readingService.saveReadingProgress(1L, 1L, 50, null, null, null, "mobile");

        assertNotNull(result);
    }

    @Test
    void getReadingProgress_exists() {
        when(readingHistoryRepository.findFirstByUserIdAndBookIdOrderByCreatedAtDesc(1L, 1L))
                .thenReturn(Optional.of(testHistory));

        ReadingProgressResponse result = readingService.getReadingProgress(1L, 1L);

        assertEquals(50, result.getPage());
        assertEquals("Chapter 3", result.getChapter());
        assertEquals(200, result.getScrollPosition());
    }

    @Test
    void getReadingProgress_notExists_returnsDefault() {
        when(readingHistoryRepository.findFirstByUserIdAndBookIdOrderByCreatedAtDesc(1L, 1L))
                .thenReturn(Optional.empty());

        ReadingProgressResponse result = readingService.getReadingProgress(1L, 1L);

        assertEquals(1, result.getPage());
        assertNull(result.getChapter());
        assertEquals(0, result.getScrollPosition());
    }

    @Test
    void getReadingHistory_success() {
        Page<ReadingHistory> page = new PageImpl<>(List.of(testHistory));
        when(readingHistoryRepository.findLatestByUserId(eq(1L), any())).thenReturn(page);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        Page<ReadingHistoryResponse> result = readingService.getReadingHistory(1L, 1, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Book", result.getContent().get(0).getBookTitle());
        assertNotNull(result.getContent().get(0).getProgress());
    }

    @Test
    void getReadingHistory_bookNotFound_throwsException() {
        Page<ReadingHistory> page = new PageImpl<>(List.of(testHistory));
        when(readingHistoryRepository.findLatestByUserId(eq(1L), any())).thenReturn(page);
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> readingService.getReadingHistory(1L, 1, 10));
    }
}