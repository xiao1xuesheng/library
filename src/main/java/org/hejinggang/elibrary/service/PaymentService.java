package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.exception.BusinessException;
import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.hejinggang.elibrary.common.enums.PayChannel;
import org.hejinggang.elibrary.model.entity.Account;
import org.hejinggang.elibrary.model.entity.AccountBalanceLog;
import org.hejinggang.elibrary.model.entity.Order;
import org.hejinggang.elibrary.model.entity.PlatformBillRecord;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.AccountBalanceLogRepository;
import org.hejinggang.elibrary.repository.AccountRepository;
import org.hejinggang.elibrary.repository.OrderRepository;
import org.hejinggang.elibrary.repository.PlatformBillRecordRepository;
import org.hejinggang.elibrary.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final AccountBalanceLogRepository balanceLogRepository;
    private final UserRepository userRepository;
    private final OperationLogService operationLogService;
    private final PlatformBillRecordRepository platformBillRecordRepository;

    public PaymentService(OrderRepository orderRepository,
                          AccountRepository accountRepository,
                          AccountBalanceLogRepository balanceLogRepository,
                          UserRepository userRepository,
                          OperationLogService operationLogService,
                          PlatformBillRecordRepository platformBillRecordRepository) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.balanceLogRepository = balanceLogRepository;
        this.userRepository = userRepository;
        this.operationLogService = operationLogService;
        this.platformBillRecordRepository = platformBillRecordRepository;
    }

    @Transactional
    public Map<String, Object> createOrder(Long userId, String productType, Long productId, BigDecimal amount) {
        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductType(productType);
        order.setProductId(productId);
        order.setAmount(amount);
        order.setStatus("PENDING");
        order.setExpireTime(LocalDateTime.now().plusMinutes(30));
        order = orderRepository.save(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("amount", order.getAmount());
        result.put("expireTime", order.getExpireTime());
        return result;
    }

    @Transactional
    public void handlePayCallback(String orderNo, String payChannel, String payOrderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在"));

        if ("PAID".equals(order.getStatus())) {
            log.warn("Order already paid: orderNo={}", orderNo);
            return;
        }

        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            order.setStatus("EXPIRED");
            orderRepository.save(order);
            throw new BusinessException(ErrorCode.ORDER_EXPIRED, "订单已过期");
        }

        order.setStatus("PAID");
        order.setPayChannel(payChannel);
        order.setPayOrderNo(payOrderNo);
        order.setPayTime(LocalDateTime.now());
        orderRepository.save(order);

        processOrder(order);
    }

    private void processOrder(Order order) {
        switch (order.getProductType()) {
            case "BALANCE":
                rechargeBalance(order);
                break;
            case "VIP_MONTH":
            case "VIP_QUARTER":
            case "VIP_YEAR":
                activateVip(order);
                break;
            default:
                log.warn("Unknown product type: {}", order.getProductType());
        }
    }

    private void rechargeBalance(Order order) {
        Account account = accountRepository.findByUserIdForUpdate(order.getUserId())
                .orElseGet(() -> createAccountIfNotExist(order.getUserId()));

        BigDecimal balanceBefore = account.getBalance();
        account.setBalance(account.getBalance().add(order.getAmount()));
        account.setTotalRecharge(account.getTotalRecharge().add(order.getAmount()));
        accountRepository.save(account);

        AccountBalanceLog balanceLog = new AccountBalanceLog();
        balanceLog.setUserId(order.getUserId());
        balanceLog.setLogType("RECHARGE");
        balanceLog.setAmount(order.getAmount());
        balanceLog.setBalanceBefore(balanceBefore);
        balanceLog.setBalanceAfter(account.getBalance());
        balanceLog.setRemark("充值");
        balanceLogRepository.save(balanceLog);
    }

    private void activateVip(Order order) {
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        int days;
        switch (order.getProductType()) {
            case "VIP_MONTH":
                days = 30;
                break;
            case "VIP_QUARTER":
                days = 90;
                break;
            case "VIP_YEAR":
                days = 365;
                break;
            default:
                days = 30;
        }

        user.setUserType(org.hejinggang.elibrary.common.enums.UserType.VIP);
        if (user.getVipExpireAt() != null && user.getVipExpireAt().isAfter(LocalDateTime.now())) {
            user.setVipExpireAt(user.getVipExpireAt().plusDays(days));
        } else {
            user.setVipExpireAt(LocalDateTime.now().plusDays(days));
        }
        userRepository.save(user);

        operationLogService.logUserOperation(user.getId(), "VIP_ACTIVATE",
                "激活VIP，产品: " + order.getProductType());
    }

    private Account createAccountIfNotExist(Long userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO);
        account.setTotalRecharge(BigDecimal.ZERO);
        account.setTotalConsume(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    /**
     * 上传第三方支付记录文件
     * @param file 对账文件（CSV格式）
     * @param payChannel 支付渠道
     * @param billDate 账单日期
     * @return 上传结果
     */
    @Transactional
    public Map<String, Object> uploadPaymentRecords(MultipartFile file, String payChannel, LocalDate billDate) {
        String uploadId = "upload-" + UUID.randomUUID().toString().substring(0, 8);
        int recordCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length < 3) {
                    log.warn("Invalid record format: {}", line);
                    continue;
                }

                PlatformBillRecord record = new PlatformBillRecord();
                record.setUploadId(uploadId);
                record.setPayChannel(PayChannel.valueOf(payChannel.toUpperCase()));
                record.setBillDate(billDate);
                record.setPlatformOrderNo(fields[0]);
                record.setLocalOrderNo(fields.length > 1 ? fields[1] : null);
                record.setAmount(new BigDecimal(fields[2]));
                record.setStatus(fields.length > 3 ? fields[3] : "UNKNOWN");
                if (fields.length > 4) {
                    try {
                        record.setPaidAt(LocalDateTime.parse(fields[4]));
                    } catch (Exception e) {
                        log.warn("Failed to parse paid time: {}", fields[4]);
                    }
                }
                record.setRawData(line);

                platformBillRecordRepository.save(record);
                recordCount++;
            }
        } catch (Exception e) {
            log.error("Failed to upload payment records: uploadId={}", uploadId, e);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件解析失败: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", uploadId);
        result.put("fileName", file.getOriginalFilename());
        result.put("recordCount", recordCount);
        result.put("status", "UPLOADED");
        return result;
    }
}