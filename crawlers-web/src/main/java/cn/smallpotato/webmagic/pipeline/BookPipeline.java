package cn.smallpotato.webmagic.pipeline;


import cn.smallpotato.entity.Book;
import cn.smallpotato.entity.Crowdfunding;
import cn.smallpotato.service.BookService;
import cn.smallpotato.service.CrowdfundingService;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * @author panjb
 */
@Component
public class BookPipeline implements Pipeline {

    private final BookService bookService;

    public BookPipeline(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Book item = resultItems.get("item");
        if (item != null) {
            Book old = this.bookService.queryByUrl(item.getUrl());
            if (old == null) {
                this.bookService.save(item);
            }
        }
    }
}
