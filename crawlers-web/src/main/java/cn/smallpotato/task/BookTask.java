package cn.smallpotato.task;

import cn.smallpotato.cases.DangDangCrawler;
import cn.smallpotato.entity.Book;
import cn.smallpotato.service.BookService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO 引入quartz，更灵活
 * 爬虫定时调度
 * @author panjb
 */
//@Component
public class BookTask {

    public final BookService bookService;

    public BookTask(BookService bookService) {
        this.bookService = bookService;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 3600 * 1000 * 10)
    public void execute() {
        Set<String> urls = bookService.list().stream().map(Book::getUrl).collect(Collectors.toSet());
        new DangDangCrawler(bookService, urls).start();
    }

}
