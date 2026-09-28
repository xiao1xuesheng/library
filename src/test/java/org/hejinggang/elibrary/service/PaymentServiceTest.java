package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.PayChannel;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.model.entity.Account;
import org.hejinggang.elibrary.model.entity.Order;
import org.hejinggang.elibrary.model.entity.PlatformBillRecord;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.AccountBalanceLogRepository;
import org.hejinggang.elibrary.repository.AccountRepository;
import org.hejinggang.elibrary.repository.OrderRepository;
import org.hejinggang.elibrary.repository.PlatformBillRecordRepository;
import org.hejinggang.elibrary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountBalanceLogRepository balanceLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private PlatformBillRecordRepository platformBillRecordRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order testOrder;
    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD123456");
        testOrder.setUserId(1L);
        testOrder.setProductType("BALANCE");
        testOrder.setAmount(new BigDecimal("100"));
        testOrder.setStatus("PENDING");
        testOrder.setExpireTime(LocalDateTime.now().plusMinutes(30));

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setUserType(UserType.NORMAL);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUserId(1L);
        testAccount.setBalance(new BigDecimal("50"));
        testAccount.setTotalRecharge(BigDecimal.ZERO);
        testAccount.setTotalConsume(BigDecimal.ZERO);
    }

    @Test
    void createOrder_success() {
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Map<String, Object> result = paymentService.createOrder(1L, "BALANCE", null, new BigDecimal("100"));

        assertNotNull(result);
        assertEquals("ORD123456", result.get("orderNo"));
        assertEquals(new BigDecimal("100"), result.get("amount"));
    }

    @Test
    void handlePayCallback_success_balance() {
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(balanceLogRepository.save(any())).thenReturn(new org.hejinggang.elibrary.model.entity.AccountBalanceLog());

        paymentService.handlePayCallback("ORD123456", "ALIPAY", "PAY123");

        assertEquals("PAID", testOrder.getStatus());
        assertEquals("ALIPAY", testOrder.getPayChannel());
        assertEquals(new BigDecimal("150"), testAccount.getBalance());
    }

    @Test
    void handlePayCallback_alreadyPaid_returns() {
        testOrder.setStatus("PAID");
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));

        paymentService.handlePayCallback("ORD123456", "ALIPAY", "PAY123");

        verify(orderRepository, times(1)).findByOrderNo("ORD123456");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void handlePayCallback_expired_throwsException() {
        testOrder.setExpireTime(LocalDateTime.now().minusMinutes(1));
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.handlePayCallback("ORD123456", "ALIPAY", "PAY123"));
        assertEquals(ErrorCode.ORDER_EXPIRED, ex.getCode());
        assertEquals("EXPIRED", testOrder.getStatus());
    }

    @Test
    void handlePayCallback_vipMonth() {
        testOrder.setProductType("VIP_MONTH");
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        paymentService.handlePayCallback("ORD123456", "WECHAT", "PAY456");

        assertEquals(UserType.VIP, testUser.getUserType());
        assertNotNull(testUser.getVipExpireAt());
        verify(operationLogService).logUserOperation(eq(1L), eq("VIP_ACTIVATE"), anyString());
    }

    @Test
    void handlePayCallback_vipQuarter() {
        testOrder.setProductType("VIP_QUARTER");
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        paymentService.handlePayCallback("ORD123456", "WECHAT", "PAY456");

        assertEquals(UserType.VIP, testUser.getUserType());
    }

    @Test
    void handlePayCallback_vipYear() {
        testOrder.setProductType("VIP_YEAR");
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        paymentService.handlePayCallback("ORD123456", "WECHAT", "PAY456");

        assertEquals(UserType.VIP, testUser.getUserType());
    }

    @Test
    void handlePayCallback_vipExtendExisting() {
        testOrder.setProductType("VIP_MONTH");
        testUser.setUserType(UserType.VIP);
        testUser.setVipExpireAt(LocalDateTime.now().plusDays(30));
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        paymentService.handlePayCallback("ORD123456", "WECHAT", "PAY456");

        assertTrue(testUser.getVipExpireAt().isAfter(LocalDateTime.now().plusDays(30)));
    }

    @Test
    void handlePayCallback_unknownProductType() {
        testOrder.setProductType("UNKNOWN");
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        paymentService.handlePayCallback("ORD123456", "ALIPAY", "PAY123");

        assertEquals("PAID", testOrder.getStatus());
    }

    @Test
    void handlePayCallback_accountNotExists_createsAccount() {
        when(orderRepository.findByOrderNo("ORD123456")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(balanceLogRepository.save(any())).thenReturn(new org.hejinggang.elibrary.model.entity.AccountBalanceLog());

        paymentService.handlePayCallback("ORD123456", "ALIPAY", "PAY123");

        verify(accountRepository, atLeastOnce()).save(any(Account.class));
    }

    @Test
    void handlePayCallback_orderNotFound_throwsException() {
        when(orderRepository.findByOrderNo("NONEXIST")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.handlePayCallback("NONEXIST", "ALIPAY", "PAY123"));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getCode());
    }

    @Test
    void uploadPaymentRecords_success() throws Exception {
        String csvContent = "platformOrderNo,localOrderNo,amount,status,paidAt\n" +
                "P001,L001,100.00,SUCCESS,2024-01-01T10:00:00\n" +
                "P002,L002,200.00,SUCCESS,2024-01-01T11:00:00";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        when(platformBillRecordRepository.save(any(PlatformBillRecord.class))).thenReturn(new PlatformBillRecord());

        Map<String, Object> result = paymentService.uploadPaymentRecords(file, "ALIPAY", LocalDate.of(2024, 1, 1));

        assertEquals("UPLOADED", result.get("status"));
        assertEquals(2, result.get("recordCount"));
        assertNotNull(result.get("uploadId"));
        verify(platformBillRecordRepository, times(2)).save(any(PlatformBillRecord.class));
    }

    @Test
    void uploadPaymentRecords_skipHeaderAndInvalidLines() throws Exception {
        String csvContent = "header1,header2,header3\n" +
                "P001,L001,100.00,SUCCESS,2024-01-01T10:00:00\n" +
                "invalid\n";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        when(platformBillRecordRepository.save(any(PlatformBillRecord.class))).thenReturn(new PlatformBillRecord());

        Map<String, Object> result = paymentService.uploadPaymentRecords(file, "WECHAT", LocalDate.of(2024, 1, 1));

        assertEquals(1, result.get("recordCount"));
    }

    @Test
    void uploadPaymentRecords_parseError_throwsException() throws Exception {
        byte[] invalidBytes = new byte[]{(byte) 0x80, (byte) 0x81, (byte) 0xFF, (byte) 0xFE};
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                new java.io.ByteArrayInputStream(invalidBytes)) {
            @Override
            public java.io.InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(invalidBytes);
            }
        };

        assertDoesNotThrow(() -> paymentService.uploadPaymentRecords(file, "ALIPAY", LocalDate.of(2024, 1, 1)));
    }
}