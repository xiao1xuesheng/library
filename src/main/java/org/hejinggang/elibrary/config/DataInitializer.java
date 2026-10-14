package org.hejinggang.elibrary.config;

import org.hejinggang.elibrary.common.enums.BookStatus;
import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.model.entity.Account;
import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.model.entity.BookAccessConfig;
import org.hejinggang.elibrary.model.entity.BorrowRecord;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.AccountRepository;
import org.hejinggang.elibrary.repository.BookAccessConfigRepository;
import org.hejinggang.elibrary.repository.BookRepository;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.hejinggang.elibrary.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BookAccessConfigRepository accessConfigRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public DataInitializer(BookRepository bookRepository, 
                          UserRepository userRepository,
                          AccountRepository accountRepository,
                          BookAccessConfigRepository accessConfigRepository,
                          BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accessConfigRepository = accessConfigRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Data already initialized, skipping...");
            return;
        }

        log.info("Initializing demo data...");

        // ============================================
        // 1. 创建演示用户
        // ============================================

        User demoUser = new User();
        demoUser.setUsername("demo_user");
        demoUser.setEmail("demo@example.com");
        demoUser.setPasswordHash("123456");
        demoUser.setUserType(UserType.NORMAL);
        demoUser.setUserStatus(UserStatus.NORMAL);
        demoUser.setBalance(new BigDecimal("100.00"));
        demoUser.setTotalRecharge(new BigDecimal("100.00"));
        demoUser.setTotalConsume(BigDecimal.ZERO);
        userRepository.save(demoUser);

        Account demoUserAccount = new Account();
        demoUserAccount.setUserId(demoUser.getId());
        demoUserAccount.setBalance(new BigDecimal("100.00"));
        demoUserAccount.setTotalRecharge(new BigDecimal("100.00"));
        demoUserAccount.setTotalConsume(BigDecimal.ZERO);
        accountRepository.save(demoUserAccount);

        User vipUser = new User();
        vipUser.setUsername("demo_vip");
        vipUser.setEmail("vip@example.com");
        vipUser.setPasswordHash("123456");
        vipUser.setUserType(UserType.VIP);
        vipUser.setUserStatus(UserStatus.NORMAL);
        vipUser.setVipExpireAt(LocalDateTime.now().plusDays(365));
        vipUser.setBalance(new BigDecimal("200.00"));
        vipUser.setTotalRecharge(new BigDecimal("200.00"));
        vipUser.setTotalConsume(BigDecimal.ZERO);
        userRepository.save(vipUser);

        Account vipUserAccount = new Account();
        vipUserAccount.setUserId(vipUser.getId());
        vipUserAccount.setBalance(new BigDecimal("200.00"));
        vipUserAccount.setTotalRecharge(new BigDecimal("200.00"));
        vipUserAccount.setTotalConsume(BigDecimal.ZERO);
        accountRepository.save(vipUserAccount);

        User adminUser = new User();
        adminUser.setUsername("demo_admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("123456");
        adminUser.setUserType(UserType.VIP);
        adminUser.setUserStatus(UserStatus.NORMAL);
        adminUser.setVipExpireAt(LocalDateTime.now().plusDays(365));
        adminUser.setBalance(new BigDecimal("500.00"));
        adminUser.setTotalRecharge(new BigDecimal("500.00"));
        adminUser.setTotalConsume(BigDecimal.ZERO);
        userRepository.save(adminUser);

        Account adminUserAccount = new Account();
        adminUserAccount.setUserId(adminUser.getId());
        adminUserAccount.setBalance(new BigDecimal("500.00"));
        adminUserAccount.setTotalRecharge(new BigDecimal("500.00"));
        adminUserAccount.setTotalConsume(BigDecimal.ZERO);
        accountRepository.save(adminUserAccount);

        // ============================================
        // 2. 创建演示书籍
        // ============================================

        Book book1 = new Book();
        book1.setTitle("Java核心技术 卷I");
        book1.setAuthor("Cay S. Horstmann");
        book1.setIsbn("978-7-111-54742-6");
        book1.setCategory("编程语言");
        book1.setDescription("Java领域最有影响力和价值的著作之一，拥有20多年教学与研究经验的资深Java技术专家撰写，全球畅销书。");
        book1.setPageCount(500);
        book1.setCoverUrl("https://example.com/covers/java-core.jpg");
        book1.setContentUrl("https://example.com/books/java-core/content.epub");
        book1.setPreviewUrl("https://example.com/books/java-core/preview.epub");
        book1.setPublishedDate(LocalDate.of(2023, 1, 15));
        book1.setStatus(BookStatus.ON_SHELF);
        book1.setRating(4.8);
        book1.setReviewCount(120);
        book1.setBorrowCount(350);
        book1.setHotScore(95);
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setTitle("Spring实战 第6版");
        book2.setAuthor("Craig Walls");
        book2.setIsbn("978-7-115-61234-5");
        book2.setCategory("框架");
        book2.setDescription("Spring框架学习必备经典，从Spring核心到Spring Boot、响应式编程，全面覆盖现代Java开发。");
        book2.setPageCount(450);
        book2.setCoverUrl("https://example.com/covers/spring-in-action.jpg");
        book2.setContentUrl("https://example.com/books/spring-in-action/content.epub");
        book2.setPreviewUrl("https://example.com/books/spring-in-action/preview.epub");
        book2.setPublishedDate(LocalDate.of(2023, 6, 1));
        book2.setStatus(BookStatus.ON_SHELF);
        book2.setRating(4.6);
        book2.setReviewCount(98);
        book2.setBorrowCount(280);
        book2.setHotScore(88);
        bookRepository.save(book2);

        Book book3 = new Book();
        book3.setTitle("设计模式：可复用面向对象软件的基础");
        book3.setAuthor("Erich Gamma等");
        book3.setIsbn("978-7-111-07575-4");
        book3.setCategory("软件工程");
        book3.setDescription("GoF经典著作，23种设计模式的权威指南，软件工程师必读经典。");
        book3.setPageCount(254);
        book3.setCoverUrl("https://example.com/covers/design-patterns.jpg");
        book3.setContentUrl("https://example.com/books/design-patterns/content.epub");
        book3.setPreviewUrl("https://example.com/books/design-patterns/preview.epub");
        book3.setPublishedDate(LocalDate.of(2022, 3, 20));
        book3.setStatus(BookStatus.ON_SHELF);
        book3.setRating(4.9);
        book3.setReviewCount(200);
        book3.setBorrowCount(500);
        book3.setHotScore(99);
        bookRepository.save(book3);

        Book book4 = new Book();
        book4.setTitle("深入理解Java虚拟机 第3版");
        book4.setAuthor("周志明");
        book4.setIsbn("978-7-111-64124-7");
        book4.setCategory("编程语言");
        book4.setDescription("全面讲解JVM原理与调优，Java开发者进阶必读，涵盖垃圾回收、内存模型、性能优化等核心主题。");
        book4.setPageCount(520);
        book4.setCoverUrl("https://example.com/covers/jvm.jpg");
        book4.setContentUrl("https://example.com/books/jvm/content.epub");
        book4.setPreviewUrl("https://example.com/books/jvm/preview.epub");
        book4.setPublishedDate(LocalDate.of(2023, 9, 10));
        book4.setStatus(BookStatus.ON_SHELF);
        book4.setRating(4.7);
        book4.setReviewCount(150);
        book4.setBorrowCount(420);
        book4.setHotScore(92);
        bookRepository.save(book4);

        Book book5 = new Book();
        book5.setTitle("数据结构与算法分析");
        book5.setAuthor("Mark Allen Weiss");
        book5.setIsbn("978-7-111-12345-6");
        book5.setCategory("计算机科学");
        book5.setDescription("经典算法教材，从基础数据结构到高级算法设计，配合Java实现，适合计算机专业学生和工程师。");
        book5.setPageCount(600);
        book5.setCoverUrl("https://example.com/covers/algorithms.jpg");
        book5.setContentUrl("https://example.com/books/algorithms/content.epub");
        book5.setPreviewUrl("https://example.com/books/algorithms/preview.epub");
        book5.setPublishedDate(LocalDate.of(2023, 2, 28));
        book5.setStatus(BookStatus.ON_SHELF);
        book5.setRating(4.5);
        book5.setReviewCount(85);
        book5.setBorrowCount(200);
        book5.setHotScore(80);
        bookRepository.save(book5);

        // ============================================
        // 3. 创建书籍访问配置
        // ============================================

        BookAccessConfig config1 = new BookAccessConfig();
        config1.setBookId(book1.getId());
        config1.setAccessType("PUBLIC_FREE");
        config1.setDailyPrice(BigDecimal.ZERO);
        config1.setVipDiscount(BigDecimal.ONE);
        config1.setMaxRenewTimes(2);
        config1.setRenewDaysFree(7);
        config1.setRenewDaysVip(15);
        accessConfigRepository.save(config1);

        BookAccessConfig config2 = new BookAccessConfig();
        config2.setBookId(book2.getId());
        config2.setAccessType("VIP_ONLY");
        config2.setDailyPrice(new BigDecimal("2.00"));
        config2.setVipDiscount(new BigDecimal("0.50"));
        config2.setMaxRenewTimes(3);
        config2.setRenewDaysFree(7);
        config2.setRenewDaysVip(15);
        accessConfigRepository.save(config2);

        BookAccessConfig config3 = new BookAccessConfig();
        config3.setBookId(book3.getId());
        config3.setAccessType("PUBLIC_FREE");
        config3.setDailyPrice(BigDecimal.ZERO);
        config3.setVipDiscount(BigDecimal.ONE);
        config3.setMaxRenewTimes(2);
        config3.setRenewDaysFree(7);
        config3.setRenewDaysVip(15);
        accessConfigRepository.save(config3);

        BookAccessConfig config4 = new BookAccessConfig();
        config4.setBookId(book4.getId());
        config4.setAccessType("PAID");
        config4.setDailyPrice(new BigDecimal("3.00"));
        config4.setVipDiscount(new BigDecimal("0.80"));
        config4.setMaxRenewTimes(2);
        config4.setRenewDaysFree(7);
        config4.setRenewDaysVip(15);
        accessConfigRepository.save(config4);

        BookAccessConfig config5 = new BookAccessConfig();
        config5.setBookId(book5.getId());
        config5.setAccessType("PUBLIC_FREE");
        config5.setDailyPrice(BigDecimal.ZERO);
        config5.setVipDiscount(BigDecimal.ONE);
        config5.setMaxRenewTimes(2);
        config5.setRenewDaysFree(7);
        config5.setRenewDaysVip(15);
        accessConfigRepository.save(config5);

        // ============================================
        // 4. 创建演示借阅记录
        // ============================================

        BorrowRecord borrow1 = new BorrowRecord();
        borrow1.setUserId(demoUser.getId());
        borrow1.setBookId(book1.getId());
        borrow1.setBorrowDate(LocalDateTime.now().minusDays(3));
        borrow1.setDueDate(LocalDateTime.now().plusDays(4));
        borrow1.setStatus(org.hejinggang.elibrary.common.enums.BorrowStatus.ACTIVE);
        borrow1.setRenewCount(0);
        borrow1.setTotalCost(BigDecimal.ZERO);
        borrow1.setRequestId("demo-borrow-001");
        borrowRecordRepository.save(borrow1);

        BorrowRecord borrow2 = new BorrowRecord();
        borrow2.setUserId(demoUser.getId());
        borrow2.setBookId(book3.getId());
        borrow2.setBorrowDate(LocalDateTime.now().minusDays(10));
        borrow2.setDueDate(LocalDateTime.now().minusDays(3));
        borrow2.setReturnDate(LocalDateTime.now().minusDays(4));
        borrow2.setStatus(org.hejinggang.elibrary.common.enums.BorrowStatus.RETURNED);
        borrow2.setRenewCount(0);
        borrow2.setTotalCost(BigDecimal.ZERO);
        borrow2.setRequestId("demo-borrow-002");
        borrowRecordRepository.save(borrow2);

        BorrowRecord borrow3 = new BorrowRecord();
        borrow3.setUserId(vipUser.getId());
        borrow3.setBookId(book2.getId());
        borrow3.setBorrowDate(LocalDateTime.now().minusDays(5));
        borrow3.setDueDate(LocalDateTime.now().plusDays(25));
        borrow3.setStatus(org.hejinggang.elibrary.common.enums.BorrowStatus.ACTIVE);
        borrow3.setRenewCount(0);
        borrow3.setTotalCost(BigDecimal.ZERO);
        borrow3.setRequestId("demo-borrow-003");
        borrowRecordRepository.save(borrow3);

        log.info("Demo data initialized successfully!");
        log.info("Users: demo_user (NORMAL, ¥100), demo_vip (VIP, ¥200), demo_admin (VIP, ¥500)");
        log.info("Books: Java核心技术, Spring实战, 设计模式, 深入理解Java虚拟机, 数据结构与算法分析");
        log.info("Borrow Records: demo_user borrowed Java核心技术 (ACTIVE), 设计模式 (RETURNED)");
        log.info("Demo credentials - Username: demo_user, Password: 123456");
    }
}