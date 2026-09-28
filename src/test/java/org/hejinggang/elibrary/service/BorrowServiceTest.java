package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.config.BorrowConfig;
import org.hejinggang.elibrary.model.dto.request.BorrowRequest;
import org.hejinggang.elibrary.model.dto.response.BorrowRecordResponse;
import org.hejinggang.elibrary.model.entity.*;
import org.hejinggang.elibrary.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountBalanceLogRepository balanceLogRepository;

    @Mock
    private BookAccessConfigRepository accessConfigRepository;

    @Mock
    private BorrowConfig borrowConfig;

    @InjectMocks
    private BorrowService borrowService;

    private User testUser;
    private Book testBook;
    private BorrowRecord testRecord;
    private Account testAccount;
    private BookAccessConfig testConfig;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setUserType(UserType.NORMAL);
        testUser.setUserStatus(UserStatus.NORMAL);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setCoverUrl("https://example.com/cover.jpg");
        testBook.setStatus(org.hejinggang.elibrary.common.enums.BookStatus.ON_SHELF);
        testBook.setBorrowCount(0);

        testRecord = new BorrowRecord();
        testRecord.setId(1L);
        testRecord.setUserId(1L);
        testRecord.setBookId(1L);
        testRecord.setStatus(BorrowStatus.ACTIVE);
        testRecord.setBorrowDate(LocalDateTime.now().minusDays(3));
        testRecord.setDueDate(LocalDateTime.now().plusDays(4));
        testRecord.setRenewCount(0);
        testRecord.setTotalCost(BigDecimal.TEN);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUserId(1L);
        testAccount.setBalance(new BigDecimal("100"));
        testAccount.setTotalRecharge(BigDecimal.ZERO);
        testAccount.setTotalConsume(BigDecimal.ZERO);

        testConfig = new BookAccessConfig();
        testConfig.setId(1L);
        testConfig.setBookId(1L);
        testConfig.setAccessType("PUBLIC_FREE");
        testConfig.setDailyPrice(BigDecimal.ZERO);
        testConfig.setVipDiscount(BigDecimal.ONE);
        testConfig.setMaxRenewTimes(2);
        testConfig.setRenewDaysFree(7);
        testConfig.setRenewDaysVip(15);
    }

    @Test
    void borrowBook_success() {
        BorrowRequest request = new BorrowRequest();
        request.setUserId(1L);
        request.setBookId(1L);
        request.setRequestId("req-001");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findByIdAndStatus(1L, org.hejinggang.elibrary.common.enums.BookStatus.ON_SHELF))
                .thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(borrowRecordRepository.countByUserIdAndStatus(1L, BorrowStatus.ACTIVE)).thenReturn(0L);
        when(accessConfigRepository.findByBookId(1L)).thenReturn(Optional.of(testConfig));
        when(borrowConfig.getMaxLimitFree()).thenReturn(3);
        when(borrowConfig.getDurationFree()).thenReturn(7);
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(testRecord);

        BorrowRecordResponse result = borrowService.borrowBook(request);

        assertNotNull(result);
        assertEquals("Test Book", result.getBookTitle());
        verify(bookRepository).save(testBook);
    }

    @Test
    void borrowBook_userDisabled_throwsException() {
        testUser.setUserStatus(UserStatus.DISABLED);
        BorrowRequest request = new BorrowRequest();
        request.setUserId(1L);
        request.setBookId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.borrowBook(request));
        assertEquals(ErrorCode.USER_DISABLED, ex.getCode());
    }

    @Test
    void borrowBook_bookOffShelf_throwsException() {
        BorrowRequest request = new BorrowRequest();
        request.setUserId(1L);
        request.setBookId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findByIdAndStatus(1L, org.hejinggang.elibrary.common.enums.BookStatus.ON_SHELF))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.borrowBook(request));
        assertEquals(ErrorCode.BOOK_OFF_SHELF, ex.getCode());
    }

    @Test
    void borrowBook_alreadyBorrowed_throwsException() {
        BorrowRequest request = new BorrowRequest();
        request.setUserId(1L);
        request.setBookId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findByIdAndStatus(1L, org.hejinggang.elibrary.common.enums.BookStatus.ON_SHELF))
                .thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.of(testRecord));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.borrowBook(request));
        assertEquals(ErrorCode.ALREADY_BORROWED, ex.getCode());
    }

    @Test
    void borrowBook_limitExceeded_throwsException() {
        BorrowRequest request = new BorrowRequest();
        request.setUserId(1L);
        request.setBookId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findByIdAndStatus(1L, org.hejinggang.elibrary.common.enums.BookStatus.ON_SHELF))
                .thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(borrowRecordRepository.countByUserIdAndStatus(1L, BorrowStatus.ACTIVE)).thenReturn(5L);
        when(borrowConfig.getMaxLimitFree()).thenReturn(3);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.borrowBook(request));
        assertEquals(ErrorCode.BORROW_LIMIT_EXCEEDED, ex.getCode());
    }

    @Test
    void borrowBook_insufficientBalance_throwsException() {
        BorrowRequest request = new BorrowRequest();
        request.setUserId(1L);
        request.setBookId(1L);

        testAccount.setBalance(BigDecimal.ZERO);
        testConfig.setAccessType("PAID");
        testConfig.setDailyPrice(new BigDecimal("10"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findByIdAndStatus(1L, org.hejinggang.elibrary.common.enums.BookStatus.ON_SHELF))
                .thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByUserIdAndBookIdAndStatus(1L, 1L, BorrowStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(borrowRecordRepository.countByUserIdAndStatus(1L, BorrowStatus.ACTIVE)).thenReturn(0L);
        when(accessConfigRepository.findByBookId(1L)).thenReturn(Optional.of(testConfig));
        when(borrowConfig.getMaxLimitFree()).thenReturn(3);
        when(borrowConfig.getDurationFree()).thenReturn(7);
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(testAccount));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.borrowBook(request));
        assertEquals(ErrorCode.BALANCE_INSUFFICIENT, ex.getCode());
    }

    @Test
    void returnBook_success() {
        when(borrowRecordRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testRecord));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(testRecord);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        BorrowRecordResponse result = borrowService.returnBook(1L, 1L);

        assertEquals(BorrowStatus.RETURNED, testRecord.getStatus());
        assertNotNull(result);
    }

    @Test
    void returnBook_notFound_throwsException() {
        when(borrowRecordRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.returnBook(1L, 1L));
        assertEquals(ErrorCode.BORROW_RECORD_NOT_FOUND, ex.getCode());
    }

    @Test
    void returnBook_alreadyReturned_throwsException() {
        testRecord.setStatus(BorrowStatus.RETURNED);
        when(borrowRecordRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testRecord));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.returnBook(1L, 1L));
        assertEquals(ErrorCode.ALREADY_RETURNED, ex.getCode());
    }

    @Test
    void renewBorrow_success() {
        when(borrowRecordRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testRecord));
        when(accessConfigRepository.findByBookId(1L)).thenReturn(Optional.of(testConfig));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(testRecord);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        BorrowRecordResponse result = borrowService.renewBorrow(1L, 1L, "renew-001");

        assertNotNull(result);
        assertEquals(1, testRecord.getRenewCount());
    }

    @Test
    void renewBorrow_renewLimitExceeded_throwsException() {
        testRecord.setRenewCount(2);
        when(borrowRecordRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testRecord));
        when(accessConfigRepository.findByBookId(1L)).thenReturn(Optional.of(testConfig));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> borrowService.renewBorrow(1L, 1L, "renew-001"));
        assertEquals(ErrorCode.RENEW_LIMIT_EXCEEDED, ex.getCode());
    }

    @Test
    void getMyBorrows_success() {
        when(borrowRecordRepository.findByUserIdAndStatus(1L, BorrowStatus.ACTIVE))
                .thenReturn(List.of(testRecord));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        List<BorrowRecordResponse> result = borrowService.getMyBorrows(1L, null);

        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getBookTitle());
    }

    @Test
    void getUserBorrowsForBos_success() {
        when(borrowRecordRepository.findByUserIdAndStatus(1L, BorrowStatus.ACTIVE))
                .thenReturn(List.of(testRecord));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        List<BorrowRecordResponse> result = borrowService.getUserBorrowsForBos(1L);

        assertEquals(1, result.size());
    }
}