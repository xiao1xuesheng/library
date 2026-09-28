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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BookService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    public BookListResponse listBooks(String keyword, String author, String category,
                                       String sortBy, String sortOrder, int page, int size) {
        Sort sort;
        if (sortBy != null) {
            sort = Sort.by("desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Page<Book> bookPage = bookRepository.findByConditions(
                keyword, author, category, BookStatus.ON_SHELF,
                PageRequest.of(page - 1, size, sort));

        BookListResponse response = new BookListResponse();
        response.setTotal(bookPage.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        response.setItems(bookPage.getContent().stream().map(this::toBookItem).collect(Collectors.toList()));
        return response;
    }

    public BookDetailResponse getBookDetail(Long bookId, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));

        BookDetailResponse response = toBookDetailResponse(book);
        response.setUserPermission(determineUserPermission(bookId, userId));
        return response;
    }

    public String determineUserPermission(Long bookId, Long userId) {
        if (userId == null) {
            return "NONE";
        }
        BorrowRecord record = borrowRecordRepository
                .findByUserIdAndBookIdAndStatus(userId, bookId, BorrowStatus.ACTIVE)
                .orElse(null);

        if (record != null && record.getDueDate().isAfter(java.time.LocalDateTime.now())) {
            return "ACTIVE";
        }
        return "NONE";
    }

    @Transactional
    public Book createBook(BookCreateRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        book.setPageCount(request.getPageCount());
        book.setCoverUrl(request.getCoverUrl());
        book.setContentUrl(request.getContentUrl());
        book.setPreviewUrl(request.getPreviewUrl());
        book.setPublishedDate(request.getPublishedDate());
        book.setStatus(BookStatus.ON_SHELF);
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long bookId, BookCreateRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        book.setPageCount(request.getPageCount());
        book.setCoverUrl(request.getCoverUrl());
        book.setContentUrl(request.getContentUrl());
        book.setPreviewUrl(request.getPreviewUrl());
        book.setPublishedDate(request.getPublishedDate());
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBookStatus(Long bookId, BookStatus status) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));
        book.setStatus(status);
        return bookRepository.save(book);
    }

    public Page<Book> listAllBooksForBos(String status, String category, int page, int size) {
        BookStatus bookStatus = status != null ? BookStatus.valueOf(status) : null;
        return bookRepository.findByConditionsForBos(null, null, category, bookStatus,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    private BookListResponse.BookItem toBookItem(Book book) {
        BookListResponse.BookItem item = new BookListResponse.BookItem();
        item.setId(book.getId());
        item.setTitle(book.getTitle());
        item.setAuthor(book.getAuthor());
        item.setCategory(book.getCategory());
        item.setCoverUrl(book.getCoverUrl());
        item.setRating(book.getRating());
        item.setReviewCount(book.getReviewCount());
        item.setPublishedDate(book.getPublishedDate());
        return item;
    }

    private BookDetailResponse toBookDetailResponse(Book book) {
        BookDetailResponse response = new BookDetailResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setCategory(book.getCategory());
        response.setDescription(book.getDescription());
        response.setPageCount(book.getPageCount());
        response.setCoverUrl(book.getCoverUrl());
        response.setRating(book.getRating());
        response.setReviewCount(book.getReviewCount());
        response.setPublishedDate(book.getPublishedDate());
        return response;
    }
}