package org.hejinggang.elibrary.config;

import org.hejinggang.elibrary.common.enums.BookStatus;
import org.hejinggang.elibrary.common.enums.UserStatus;
import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.BookRepository;
import org.hejinggang.elibrary.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public DataInitializer(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Data already initialized, skipping...");
            return;
        }

        log.info("Initializing sample data...");

        User freeUser = new User();
        freeUser.setUsername("freeuser");
        freeUser.setEmail("free@example.com");
        freeUser.setPasswordHash("123456");
        freeUser.setUserType(UserType.NORMAL);
        freeUser.setUserStatus(UserStatus.NORMAL);
        userRepository.save(freeUser);

        User vipUser = new User();
        vipUser.setUsername("vipuser");
        vipUser.setEmail("vip@example.com");
        vipUser.setPasswordHash("123456");
        vipUser.setUserType(UserType.VIP);
        vipUser.setUserStatus(UserStatus.NORMAL);
        userRepository.save(vipUser);

        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPasswordHash("123456");
        adminUser.setUserType(UserType.VIP);
        adminUser.setUserStatus(UserStatus.NORMAL);
        userRepository.save(adminUser);

        Book book1 = new Book();
        book1.setTitle("Java Core Technology");
        book1.setAuthor("Cay S. Horstmann");
        book1.setIsbn("978-7-111-00001-1");
        book1.setCategory("Technology");
        book1.setDescription("A comprehensive guide to Java programming.");
        book1.setPageCount(500);
        book1.setCoverUrl("https://example.com/covers/java-core.jpg");
        book1.setContentUrl("https://example.com/books/java-core/content.epub");
        book1.setPreviewUrl("https://example.com/books/java-core/preview.epub");
        book1.setPublishedDate(LocalDate.of(2023, 1, 15));
        book1.setStatus(BookStatus.ON_SHELF);
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setTitle("Spring in Action");
        book2.setAuthor("Craig Walls");
        book2.setIsbn("978-7-111-00002-2");
        book2.setCategory("Technology");
        book2.setDescription("Learn Spring Framework from scratch.");
        book2.setPageCount(400);
        book2.setCoverUrl("https://example.com/covers/spring-in-action.jpg");
        book2.setContentUrl("https://example.com/books/spring-in-action/content.epub");
        book2.setPreviewUrl("https://example.com/books/spring-in-action/preview.epub");
        book2.setPublishedDate(LocalDate.of(2023, 6, 1));
        book2.setStatus(BookStatus.ON_SHELF);
        bookRepository.save(book2);

        Book book3 = new Book();
        book3.setTitle("Design Patterns");
        book3.setAuthor("Gang of Four");
        book3.setIsbn("978-7-111-00003-3");
        book3.setCategory("Technology");
        book3.setDescription("Classic book on software design patterns.");
        book3.setPageCount(350);
        book3.setCoverUrl("https://example.com/covers/design-patterns.jpg");
        book3.setContentUrl("https://example.com/books/design-patterns/content.epub");
        book3.setPreviewUrl("https://example.com/books/design-patterns/preview.epub");
        book3.setPublishedDate(LocalDate.of(2022, 3, 20));
        book3.setStatus(BookStatus.ON_SHELF);
        bookRepository.save(book3);

        log.info("Sample data initialized successfully!");
        log.info("Users: freeuser (NORMAL), vipuser (VIP), admin (VIP)");
        log.info("Books: Java Core Technology, Spring in Action, Design Patterns");
    }
}