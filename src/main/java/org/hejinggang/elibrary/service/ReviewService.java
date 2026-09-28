package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.dto.request.ReviewRequest;
import org.hejinggang.elibrary.model.dto.response.ReviewResponse;
import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.model.entity.Review;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.BookRepository;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.hejinggang.elibrary.repository.ReviewRepository;
import org.hejinggang.elibrary.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         BorrowRecordRepository borrowRecordRepository,
                         UserRepository userRepository,
                         BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public ReviewResponse createReview(Long bookId, ReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        if (user.getUserStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "User has been disabled");
        }

        if (user.getUserStatus() == UserStatus.MUTED) {
            throw new BusinessException(ErrorCode.USER_MUTED, "User has been muted");
        }

        borrowRecordRepository.findByUserIdAndBookIdAndStatus(
                        request.getUserId(), bookId, BorrowStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_BORROW_PERMISSION,
                        "Must borrow the book before reviewing"));

        Review review = new Review();
        review.setUserId(request.getUserId());
        review.setBookId(bookId);
        review.setContent(request.getContent());
        review.setPageReference(request.getPageReference());
        review.setRating(request.getRating());
        review.setRequestId(request.getRequestId());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        review = reviewRepository.save(review);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));
        book.setReviewCount(book.getReviewCount() + 1);
        bookRepository.save(book);

        return toResponse(review, user.getUsername());
    }

    public Page<ReviewResponse> getReviewsByBookId(Long bookId, int page, int size) {
        Page<Review> reviewPage = reviewRepository.findByBookIdAndIsDeletedFalse(bookId,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return reviewPage.map(review -> {
            User user = userRepository.findById(review.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));
            return toResponse(review, user.getUsername());
        });
    }

    public Page<ReviewResponse> getAllReviewsForBos(Long bookId, int page, int size) {
        Page<Review> reviewPage;
        if (bookId != null) {
            reviewPage = reviewRepository.findByBookIdAndIsDeletedFalse(bookId,
                    PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        } else {
            reviewPage = reviewRepository.findByIsDeletedFalse(
                    PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        }

        return reviewPage.map(review -> {
            User user = userRepository.findById(review.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));
            return toResponse(review, user.getUsername());
        });
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND, "Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_REVIEW, "Not your review");
        }

        if (review.getIsDeleted()) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND, "Review has been deleted");
        }

        review.setContent(content);
        review.setUpdatedAt(LocalDateTime.now());
        review = reviewRepository.save(review);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        return toResponse(review, user.getUsername());
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND, "Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_REVIEW, "Not your review");
        }

        softDeleteReview(review, null, null);
    }

    @Transactional
    public void deleteReviewByAdmin(Long reviewId, Long adminId, String reason) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND, "Review not found"));

        softDeleteReview(review, adminId, reason);
    }

    private void softDeleteReview(Review review, Long adminId, String reason) {
        Book book = bookRepository.findById(review.getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));
        book.setReviewCount(Math.max(0, book.getReviewCount() - 1));
        bookRepository.save(book);

        review.setIsDeleted(true);
        review.setDeletedAt(LocalDateTime.now());
        review.setDeletedBy(adminId);
        review.setDeleteReason(reason);
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    private ReviewResponse toResponse(Review review, String username) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUserId());
        response.setUsername(username);
        response.setContent(review.getContent());
        response.setPageReference(review.getPageReference());
        response.setRating(review.getRating());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}