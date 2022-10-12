package cn.smallpotato.task;

import cn.smallpotato.webmagic.pipeline.NatoDocFilePipeline;
import cn.smallpotato.webmagic.pipeline.NatoDocPipeline;
import cn.smallpotato.webmagic.processor.NatoDocPageProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

/**
 * TODO 引入quartz，更灵活
 * 爬虫定时调度
 * @author panjb
 */
@Component
public class NatoDocTask {

    public final NatoDocPipeline natoDocPipeline;

    public NatoDocTask(NatoDocPipeline natoDocPipeline) {
        this.natoDocPipeline = natoDocPipeline;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 3600 * 1000 * 10)
    public void execute() {
        Spider.create(new NatoDocPageProcessor())
                .addUrl("https://www.nato.int/cps/en/natohq/official_texts.htm?search_types=Official%20text&display_mode=official_text&date_from=01.01.1950&date_to=31.12.2013&keywordquery=*&chunk=1")
                .thread(8)
                .addPipeline(new NatoDocFilePipeline())
                .start();

    }

}
