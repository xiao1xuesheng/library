package org.hejinggang.elibrary.controller.bos;

import org.hejinggang.elibrary.common.response.ApiResponse;
import org.hejinggang.elibrary.model.entity.Account;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.repository.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台数据看板接口
 */
@RestController
@RequestMapping("/bos/dashboard")
public class BosDashboardController {

    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final AccountRepository accountRepository;
    private final BookRepository bookRepository;

    public BosDashboardController(UserRepository userRepository,
                                  BorrowRecordRepository borrowRecordRepository,
                                  AccountRepository accountRepository,
                                  BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.accountRepository = accountRepository;
        this.bookRepository = bookRepository;
    }

    /**
     * 获取系统概览数据
     * @return 系统概览数据（用户总量、VIP数量、盈利数量等）
     */
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> getOverview() {
        Map<String, Object> data = new HashMap<>();

        long totalUsers = userRepository.count();
        long vipUsers = userRepository.countByUserType(UserType.VIP);
        long normalUsers = userRepository.countByUserType(UserType.NORMAL);

        List<Account> allAccounts = accountRepository.findAll();
        BigDecimal totalRecharge = allAccounts.stream()
                .map(Account::getTotalRecharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalConsume = allAccounts.stream()
                .map(Account::getTotalConsume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.put("totalUsers", totalUsers);
        data.put("vipUsers", vipUsers);
        data.put("normalUsers", normalUsers);
        data.put("totalRecharge", totalRecharge);
        data.put("totalConsume", totalConsume);
        data.put("totalProfit", totalRecharge);

        return ApiResponse.success(data);
    }

    /**
     * 获取日活数据
     * @param days 天数
     * @return 日活数据
     */
    @GetMapping("/dau")
    public ApiResponse<Map<String, Object>> getDau(@RequestParam(defaultValue = "7") int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> data = new HashMap<>();
        data.put("dau", borrowRecordRepository.countByBorrowDateAfter(since));
        return ApiResponse.success(data);
    }

    /**
     * 获取热门书籍TOP10
     * @return 热门书籍列表
     */
    @GetMapping("/hot-books")
    public ApiResponse<List<Map<String, Object>>> getHotBooks() {
        List<User> topBorrowedBooks = userRepository.findAll();
        List<Map<String, Object>> result = topBorrowedBooks.stream()
                .limit(10)
                .map(user -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", user.getId());
                    item.put("username", user.getUsername());
                    return item;
                })
                .toList();
        return ApiResponse.success(result);
    }
}