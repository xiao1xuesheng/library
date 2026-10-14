package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.config.BorrowConfig;
import org.hejinggang.elibrary.common.enums.BookStatus;
import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.dto.request.BorrowRequest;
import org.hejinggang.elibrary.model.dto.response.BorrowRecordResponse;
import org.hejinggang.elibrary.model.entity.*;
import org.hejinggang.elibrary.repository.*;
import org.hejinggang.elibrary.service.pricing.PricingStrategy;
import org.hejinggang.elibrary.service.pricing.PricingStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountBalanceLogRepository balanceLogRepository;
    private final BookAccessConfigRepository accessConfigRepository;
    private final BorrowConfig borrowConfig;
    private final PricingStrategyFactory pricingStrategyFactory;

    public BorrowService(BorrowRecordRepository borrowRecordRepository,
                         BookRepository bookRepository,
                         UserRepository userRepository,
                         AccountRepository accountRepository,
                         AccountBalanceLogRepository balanceLogRepository,
                         BookAccessConfigRepository accessConfigRepository,
                         BorrowConfig borrowConfig,
                         PricingStrategyFactory pricingStrategyFactory) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.balanceLogRepository = balanceLogRepository;
        this.accessConfigRepository = accessConfigRepository;
        this.borrowConfig = borrowConfig;
        this.pricingStrategyFactory = pricingStrategyFactory;
    }

    @Transactional
    public BorrowRecordResponse borrowBook(BorrowRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        if (user.getUserStatus() == UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "User has been disabled");
        }

        Book book = bookRepository.findByIdAndStatus(request.getBookId(), BookStatus.ON_SHELF)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_OFF_SHELF, "Book not found or off shelf"));

        borrowRecordRepository.findByUserIdAndBookIdAndStatus(
                        request.getUserId(), request.getBookId(), BorrowStatus.ACTIVE)
                .ifPresent(r -> {
                    throw new BusinessException(ErrorCode.ALREADY_BORROWED, "Already borrowed this book");
                });

        long activeCount = borrowRecordRepository.countByUserIdAndStatus(
                request.getUserId(), BorrowStatus.ACTIVE);
        int maxLimit = user.getUserType() == UserType.VIP ? borrowConfig.getMaxLimitVip() : borrowConfig.getMaxLimitFree();
        if (activeCount >= maxLimit) {
            throw new BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED, "Borrow limit exceeded");
        }

        BookAccessConfig config = accessConfigRepository.findByBookId(book.getId())
                .orElseGet(() -> {
                    BookAccessConfig defaultConfig = new BookAccessConfig();
                    defaultConfig.setBookId(book.getId());
                    defaultConfig.setAccessType("PUBLIC_FREE");
                    defaultConfig.setDailyPrice(BigDecimal.ZERO);
                    defaultConfig.setVipDiscount(BigDecimal.ONE);
                    defaultConfig.setMaxRenewTimes(2);
                    defaultConfig.setRenewDaysFree(7);
                    defaultConfig.setRenewDaysVip(15);
                    return accessConfigRepository.save(defaultConfig);
                });

        PricingStrategy pricingStrategy = pricingStrategyFactory.getStrategy(config.getAccessType());
        BigDecimal dailyCost = pricingStrategy.calculateDailyCost(config, user);
        int borrowDays = user.getUserType() == UserType.VIP ? borrowConfig.getDurationVip() : borrowConfig.getDurationFree();
        BigDecimal totalCost = dailyCost.multiply(new BigDecimal(borrowDays));

        Account account = accountRepository.findByUserIdForUpdate(user.getId())
                .orElseGet(() -> createAccountIfNotExist(user.getId()));

        if (account.getBalance().compareTo(totalCost) < 0) {
            throw new BusinessException(ErrorCode.BALANCE_INSUFFICIENT, "Balance insufficient");
        }

        BigDecimal balanceBefore = account.getBalance();
        account.setBalance(account.getBalance().subtract(totalCost));
        account.setTotalConsume(account.getTotalConsume().add(totalCost));
        accountRepository.save(account);

        AccountBalanceLog log = new AccountBalanceLog();
        log.setUserId(user.getId());
        log.setLogType("BORROW_DEDUCT");
        log.setAmount(totalCost.negate());
        log.setBalanceBefore(balanceBefore);
        log.setBalanceAfter(account.getBalance());
        log.setRemark("借阅《" + book.getTitle() + "》扣费");
        balanceLogRepository.save(log);

        BorrowRecord record = new BorrowRecord();
        record.setUserId(request.getUserId());
        record.setBookId(request.getBookId());
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(borrowDays));
        record.setStatus(BorrowStatus.ACTIVE);
        record.setTotalCost(totalCost);
        record.setRequestId(request.getRequestId());
        record = borrowRecordRepository.save(record);

        book.setBorrowCount(book.getBorrowCount() + 1);
        bookRepository.save(book);

        return toResponse(record, book.getTitle(), book.getCoverUrl());
    }

    @Transactional
    public BorrowRecordResponse returnBook(Long borrowId, Long userId) {
        BorrowRecord record = borrowRecordRepository.findByIdAndUserId(borrowId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND, "Borrow record not found"));

        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED, "Already returned");
        }

        record.setStatus(BorrowStatus.RETURNED);
        record.setReturnDate(LocalDateTime.now());
        record = borrowRecordRepository.save(record);

        Book book = bookRepository.findById(record.getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));

        return toResponse(record, book.getTitle(), book.getCoverUrl());
    }

    @Transactional
    public BorrowRecordResponse renewBorrow(Long borrowId, Long userId, String requestId) {
        BorrowRecord record = borrowRecordRepository.findByIdAndUserId(borrowId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_RECORD_NOT_FOUND, "Borrow record not found"));

        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED, "Cannot renew returned book");
        }

        BookAccessConfig config = accessConfigRepository.findByBookId(record.getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book access config not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));

        int maxRenewTimes = config.getMaxRenewTimes();
        if (record.getRenewCount() >= maxRenewTimes) {
            throw new BusinessException(ErrorCode.RENEW_LIMIT_EXCEEDED, "Renew limit exceeded");
        }

        PricingStrategy pricingStrategy = pricingStrategyFactory.getStrategy(config.getAccessType());
        int renewDays = pricingStrategy.calculateRenewDays(config, user);
        BigDecimal dailyCost = pricingStrategy.calculateDailyCost(config, user);
        BigDecimal renewCost = dailyCost.multiply(new BigDecimal(renewDays));

        Account account = accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Account not found"));

        if (account.getBalance().compareTo(renewCost) < 0) {
            throw new BusinessException(ErrorCode.BALANCE_INSUFFICIENT, "Balance insufficient for renew");
        }

        BigDecimal balanceBefore = account.getBalance();
        account.setBalance(account.getBalance().subtract(renewCost));
        account.setTotalConsume(account.getTotalConsume().add(renewCost));
        accountRepository.save(account);

        AccountBalanceLog log = new AccountBalanceLog();
        log.setUserId(userId);
        log.setLogType("RENEW_DEDUCT");
        log.setAmount(renewCost.negate());
        log.setBalanceBefore(balanceBefore);
        log.setBalanceAfter(account.getBalance());
        log.setRelatedBorrowId(borrowId);
        log.setRemark("借阅续期扣费");
        balanceLogRepository.save(log);

        record.setRenewCount(record.getRenewCount() + 1);
        record.setDueDate(record.getDueDate().plusDays(renewDays));
        record.setTotalCost(record.getTotalCost().add(renewCost));
        record.setRequestId(requestId);
        record = borrowRecordRepository.save(record);

        Book book = bookRepository.findById(record.getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));

        return toResponse(record, book.getTitle(), book.getCoverUrl());
    }

    public List<BorrowRecordResponse> getMyBorrows(Long userId, String status) {
        BorrowStatus borrowStatus = status != null ? BorrowStatus.valueOf(status) : BorrowStatus.ACTIVE;
        List<BorrowRecord> records = borrowRecordRepository.findByUserIdAndStatus(userId, borrowStatus);

        return records.stream().map(record -> {
            Book book = bookRepository.findById(record.getBookId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));
            return toResponse(record, book.getTitle(), book.getCoverUrl());
        }).collect(Collectors.toList());
    }

    public List<BorrowRecordResponse> getUserBorrowsForBos(Long userId) {
        List<BorrowRecord> records = borrowRecordRepository.findByUserIdAndStatus(userId, BorrowStatus.ACTIVE);
        return records.stream().map(record -> {
            Book book = bookRepository.findById(record.getBookId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Book not found"));
            return toResponse(record, book.getTitle(), book.getCoverUrl());
        }).collect(Collectors.toList());
    }

    private Account createAccountIfNotExist(Long userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        account.setTotalRecharge(BigDecimal.ZERO);
        account.setTotalConsume(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    private BorrowRecordResponse toResponse(BorrowRecord record, String bookTitle, String coverUrl) {
        BorrowRecordResponse response = new BorrowRecordResponse();
        response.setId(record.getId());
        response.setBookId(record.getBookId());
        response.setBookTitle(bookTitle);
        response.setCoverUrl(coverUrl);
        response.setBorrowDate(record.getBorrowDate());
        response.setDueDate(record.getDueDate());
        response.setReturnDate(record.getReturnDate());
        response.setStatus(record.getStatus().name());
        response.setRenewCount(record.getRenewCount());
        response.setTotalCost(record.getTotalCost());

        if (record.getStatus() == BorrowStatus.ACTIVE) {
            long days = Duration.between(LocalDateTime.now(), record.getDueDate()).toDays();
            response.setRemainingDays(days);
        }
        return response;
    }
}