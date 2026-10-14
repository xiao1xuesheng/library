package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.dto.response.ReadingHistoryResponse;
import org.hejinggang.elibrary.model.dto.response.ReadingProgressResponse;
import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.model.entity.ReadingHistory;
import org.hejinggang.elibrary.repository.BookRepository;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.hejinggang.elibrary.repository.ReadingHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;

    public ReadingService(ReadingHistoryRepository readingHistoryRepository,
                          BorrowRecordRepository borrowRecordRepository,
                          BookRepository bookRepository) {
        this.readingHistoryRepository = readingHistoryRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public ReadingProgressResponse saveReadingProgress(Long userId, Long bookId, Integer page,
                                                       String chapter, Integer scrollPosition,
                                                       Integer readDuration, String deviceType) {
        boolean hasBorrowed = borrowRecordRepository
                .findByUserIdAndBookIdAndStatus(userId, bookId, BorrowStatus.ACTIVE)
                .isPresent();

        if (!hasBorrowed) {
            throw new BusinessException(ErrorCode.NO_READ_PERMISSION, "Must borrow the book before reading");
        }

        ReadingHistory history = new ReadingHistory();
        history.setUserId(userId);
        history.setBookId(bookId);
        history.setPage(page);
        history.setChapter(chapter);
        history.setScrollPosition(scrollPosition);
        history.setReadDuration(readDuration != null ? readDuration : 0);
        history.setDeviceType(deviceType);
        history = readingHistoryRepository.save(history);

        ReadingProgressResponse response = new ReadingProgressResponse();
        response.setUserId(userId);
        response.setBookId(bookId);
        response.setPage(page);
        response.setChapter(chapter);
        response.setScrollPosition(scrollPosition);
        response.setUpdatedAt(history.getCreatedAt());
        return response;
    }

    public ReadingProgressResponse getReadingProgress(Long userId, Long bookId) {
        return readingHistoryRepository.findFirstByUserIdAndBookIdOrderByCreatedAtDesc(userId, bookId)
                .map(history -> {
                    ReadingProgressResponse response = new ReadingProgressResponse();
                    response.setUserId(userId);
                    response.setBookId(bookId);
                    response.setPage(history.getPage());
                    response.setChapter(history.getChapter());
                    response.setScrollPosition(history.getScrollPosition());
                    response.setUpdatedAt(history.getCreatedAt());
                    return response;
                })
                .orElseGet(() -> {
                    ReadingProgressResponse response = new ReadingProgressResponse();
                    response.setUserId(userId);
                    response.setBookId(bookId);
                    response.setPage(1);
                    response.setChapter(null);
                    response.setScrollPosition(0);
                    return response;
                });
    }

    public Page<ReadingHistoryResponse> getReadingHistory(Long userId, int page, int size) {
        Page<ReadingHistory> historyPage = readingHistoryRepository
                .findLatestByUserId(userId, PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return historyPage.map(history -> {
            Book book = bookRepository.findById(history.getBookId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));

            ReadingHistoryResponse response = new ReadingHistoryResponse();
            response.setId(history.getId());
            response.setBookId(history.getBookId());
            response.setBookTitle(book.getTitle());
            response.setBookCoverUrl(book.getCoverUrl());
            response.setLastPage(history.getPage());
            response.setLastChapter(history.getChapter());
            response.setTotalReadDuration(history.getTotalReadDuration());
            response.setLastReadAt(history.getCreatedAt());

            if (book.getPageCount() != null && book.getPageCount() > 0) {
                response.setProgress((double) history.getPage() / book.getPageCount());
            }

            return response;
        });
    }
}