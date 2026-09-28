package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.dto.request.ReviewRequest;
import org.hejinggang.elibrary.model.dto.response.ReviewResponse;
import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.model.entity.BorrowRecord;
import org.hejinggang.elibrary.model.entity.Review;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.BookRepository;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.hejinggang.elibrary.repository.ReviewRepository;
import org.hejinggang.elibrary.repository.UserRepository;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Book testBook;
    private Review testReview;
    private BorrowRecord testBorrowRecord;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setUserStatus(UserStatus.NORMAL);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setReviewCount(5);

        testReview = new Review();
        testReview.setId(1L);
        testReview.setUserId(1L);
        testReview.setBookId(1L);
        testReview.setContent("Great book!");
        testReview.setRating(5);
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());
        testReview.setIsDeleted(false);

        testBorrowRecord = new BorrowRecord();
        testBorrowRecord.setId(1L);
        testBorrowRecord.setUserId(1L);
        testBorrowRecord.setBookId(1L);
        testBorrowRecord.setStatus(BorrowStatus.ACTIVE);
        testBorrowRecord.setDueDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    void createReview_success() {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(1L);
        request.setContent("Great book!");
        request.setRating(5);
        request.setRequestId("req-001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.of(testBorrowRecord));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        ReviewResponse result = reviewService.createReview(1L, request);

        assertNotNull(result);
        assertEquals("Great book!", result.getContent());
        verify(bookRepository).save(testBook);
        assertEquals(6, testBook.getReviewCount());
    }

    @Test
    void createReview_userDisabled_throwsException() {
        testUser.setUserStatus(UserStatus.DISABLED);
        ReviewRequest request = new ReviewRequest();
        request.setUserId(1L);
        request.setContent("Review");
        request.setRequestId("req-001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.createReview(1L, request));
        assertEquals(ErrorCode.USER_DISABLED, ex.getCode());
    }

    @Test
    void createReview_userMuted_throwsException() {
        testUser.setUserStatus(UserStatus.MUTED);
        ReviewRequest request = new ReviewRequest();
        request.setUserId(1L);
        request.setContent("Review");
        request.setRequestId("req-001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.createReview(1L, request));
        assertEquals(ErrorCode.USER_MUTED, ex.getCode());
    }

    @Test
    void createReview_noBorrowPermission_throwsException() {
        ReviewRequest request = new ReviewRequest();
        request.setUserId(1L);
        request.setContent("Review");
        request.setRequestId("req-001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.createReview(1L, request));
        assertEquals(ErrorCode.NO_BORROW_PERMISSION, ex.getCode());
    }

    @Test
    void getReviewsByBookId_success() {
        Page<Review> page = new PageImpl<>(List.of(testReview));
        when(reviewRepository.findByBookIdAndIsDeletedFalse(eq(1L), any())).thenReturn(page);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Page<ReviewResponse> result = reviewService.getReviewsByBookId(1L, 1, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    @Test
    void getAllReviewsForBos_withBookId() {
        Page<Review> page = new PageImpl<>(List.of(testReview));
        when(reviewRepository.findByBookIdAndIsDeletedFalse(eq(1L), any())).thenReturn(page);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Page<ReviewResponse> result = reviewService.getAllReviewsForBos(1L, 1, 10);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllReviewsForBos_withoutBookId() {
        Page<Review> page = new PageImpl<>(List.of(testReview));
        when(reviewRepository.findByIsDeletedFalse(any())).thenReturn(page);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Page<ReviewResponse> result = reviewService.getAllReviewsForBos(null, 1, 10);

        assertEquals(1, result.getTotalElements());
        verify(reviewRepository).findByIsDeletedFalse(any());
    }

    @Test
    void updateReview_success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ReviewResponse result = reviewService.updateReview(1L, 1L, "Updated content");

        assertEquals("Updated content", testReview.getContent());
        assertNotNull(result);
    }

    @Test
    void updateReview_notFound_throwsException() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.updateReview(999L, 1L, "Updated"));
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, ex.getCode());
    }

    @Test
    void updateReview_notOwner_throwsException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.updateReview(1L, 2L, "Updated"));
        assertEquals(ErrorCode.NOT_YOUR_REVIEW, ex.getCode());
    }

    @Test
    void updateReview_deleted_throwsException() {
        testReview.setIsDeleted(true);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.updateReview(1L, 1L, "Updated"));
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, ex.getCode());
    }

    @Test
    void deleteReview_success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        reviewService.deleteReview(1L, 1L);

        assertTrue(testReview.getIsDeleted());
        assertEquals(4, testBook.getReviewCount());
    }

    @Test
    void deleteReview_notOwner_throwsException() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> reviewService.deleteReview(1L, 2L));
        assertEquals(ErrorCode.NOT_YOUR_REVIEW, ex.getCode());
    }

    @Test
    void deleteReviewByAdmin_success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        reviewService.deleteReviewByAdmin(1L, 100L, "Inappropriate content");

        assertTrue(testReview.getIsDeleted());
        assertEquals(100L, testReview.getDeletedBy());
        assertEquals("Inappropriate content", testReview.getDeleteReason());
    }
}