package cn.smallpotato.task;

import cn.smallpotato.webmagic.pipeline.BookPipeline;
import cn.smallpotato.webmagic.processor.BookPageProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

/**
 * TODO 引入quartz，更灵活
 * 爬虫定时调度
 * @author panjb
 */
@Component
public class BookTask {

    private final BookPipeline bookPipeline;

    public BookTask(BookPipeline bookPipeline) {
        this.bookPipeline = bookPipeline;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 3600 * 1000)
    public void execute() {
        Spider.create(new BookPageProcessor())
                .addUrl("http://category.dangdang.com/pg1-cp01.27.01.13.00.00.html")
                .addPipeline(this.bookPipeline)
                .thread(1)
                .run();
    }
}
