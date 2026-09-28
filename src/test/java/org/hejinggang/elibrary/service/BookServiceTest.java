package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.BookStatus;
import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.dto.request.BookCreateRequest;
import org.hejinggang.elibrary.model.dto.response.BookDetailResponse;
import org.hejinggang.elibrary.model.dto.response.BookListResponse;
import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.model.entity.BorrowRecord;
import org.hejinggang.elibrary.repository.BookRepository;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("978-0000000000");
        testBook.setCategory("Tech");
        testBook.setDescription("A test book");
        testBook.setPageCount(300);
        testBook.setCoverUrl("https://example.com/cover.jpg");
        testBook.setStatus(BookStatus.ON_SHELF);
        testBook.setRating(4.5);
        testBook.setReviewCount(10);
        testBook.setBorrowCount(5);
        testBook.setPublishedDate(LocalDate.of(2023, 1, 1));
    }

    @Test
    void listBooks_success() {
        Page<Book> page = new PageImpl<>(List.of(testBook));
        when(bookRepository.findByConditions(any(), any(), any(), eq(BookStatus.ON_SHELF), any())).thenReturn(page);

        BookListResponse result = bookService.listBooks("test", null, null, "createdAt", "desc", 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("Test Book", result.getItems().get(0).getTitle());
    }

    @Test
    void getBookDetail_withActiveBorrow() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setBookId(1L);
        record.setStatus(BorrowStatus.ACTIVE);
        record.setDueDate(LocalDateTime.now().plusDays(7));

        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.of(record));

        BookDetailResponse result = bookService.getBookDetail(1L, 1L);

        assertEquals("Test Book", result.getTitle());
        assertEquals("ACTIVE", result.getUserPermission());
    }

    @Test
    void getBookDetail_noBorrow() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.empty());

        BookDetailResponse result = bookService.getBookDetail(1L, 1L);

        assertEquals("NONE", result.getUserPermission());
    }

    @Test
    void getBookDetail_nullUser() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        BookDetailResponse result = bookService.getBookDetail(1L, null);

        assertEquals("NONE", result.getUserPermission());
    }

    @Test
    void getBookDetail_notFound_throwsException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookService.getBookDetail(999L, 1L));
        assertEquals(ErrorCode.NOT_FOUND, ex.getCode());
    }

    @Test
    void createBook_success() {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Book");
        request.setAuthor("New Author");
        request.setIsbn("978-0000000001");
        request.setCategory("Tech");
        request.setDescription("New desc");
        request.setPageCount(200);
        request.setPublishedDate(LocalDate.of(2023, 6, 1));

        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book result = bookService.createBook(request);

        assertEquals("Test Book", result.getTitle());
        assertEquals(BookStatus.ON_SHELF, result.getStatus());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_success() {
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("Updated Title");
        request.setAuthor("Updated Author");
        request.setIsbn("978-0000000002");
        request.setPageCount(250);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book result = bookService.updateBook(1L, request);

        assertEquals("Updated Title", testBook.getTitle());
        assertEquals("Updated Author", testBook.getAuthor());
    }

    @Test
    void updateBook_notFound_throwsException() {
        BookCreateRequest request = new BookCreateRequest();
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookService.updateBook(999L, request));
        assertEquals(ErrorCode.NOT_FOUND, ex.getCode());
    }

    @Test
    void updateBookStatus_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book result = bookService.updateBookStatus(1L, BookStatus.OFF_SHELF);

        assertEquals(BookStatus.OFF_SHELF, testBook.getStatus());
    }

    @Test
    void updateBookStatus_notFound_throwsException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookService.updateBookStatus(999L, BookStatus.OFF_SHELF));
        assertEquals(ErrorCode.NOT_FOUND, ex.getCode());
    }

    @Test
    void listAllBooksForBos_withStatus() {
        Page<Book> page = new PageImpl<>(List.of(testBook));
        when(bookRepository.findByConditionsForBos(any(), any(), any(), any(), any())).thenReturn(page);

        Page<Book> result = bookService.listAllBooksForBos("ON_SHELF", "Tech", 1, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void listAllBooksForBos_noFilters() {
        Page<Book> page = new PageImpl<>(List.of(testBook));
        when(bookRepository.findByConditionsForBos(any(), any(), any(), any(), any())).thenReturn(page);

        Page<Book> result = bookService.listAllBooksForBos(null, null, 1, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void determineUserPermission_expiredBorrow() {
        BorrowRecord record = new BorrowRecord();
        record.setDueDate(LocalDateTime.now().minusDays(1));
        record.setStatus(BorrowStatus.ACTIVE);

        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.of(record));

        String permission = bookService.determineUserPermission(1L, 1L);

        assertEquals("NONE", permission);
    }
}