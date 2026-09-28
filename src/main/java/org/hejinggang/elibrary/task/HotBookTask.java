package org.hejinggang.elibrary.task;

import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.repository.BookRepository;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.hejinggang.elibrary.util.DistributedLockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HotBookTask {

    private static final Logger log = LoggerFactory.getLogger(HotBookTask.class);

    private static final String LOCK_KEY = "task:calculate_hot_books";
    private static final long LOCK_EXPIRE_SECONDS = 600;

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final DistributedLockUtil lockUtil;

    public HotBookTask(BookRepository bookRepository,
                       BorrowRecordRepository borrowRecordRepository,
                       DistributedLockUtil lockUtil) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.lockUtil = lockUtil;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void calculateHotBooks() {
        lockUtil.executeWithLock(LOCK_KEY, LOCK_EXPIRE_SECONDS, () -> {
            log.info("开始计算热门书籍...");
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            
            List<Book> allBooks = bookRepository.findAll(
                    PageRequest.of(0, 1000, Sort.by("id").ascending())
            ).getContent();

            for (Book book : allBooks) {
                try {
                    long recentBorrows = borrowRecordRepository.countByBookIdAndBorrowDateAfter(
                            book.getId(), thirtyDaysAgo);
                    int hotScore = (int) (recentBorrows * 10 + book.getRating() * book.getBorrowCount());
                    book.setHotScore(hotScore);
                    bookRepository.save(book);
                } catch (Exception e) {
                    log.error("计算热门书籍失败: bookId={}", book.getId(), e);
                }
            }
            log.info("计算热门书籍完成，共处理{}本书", allBooks.size());
        });
    }
}