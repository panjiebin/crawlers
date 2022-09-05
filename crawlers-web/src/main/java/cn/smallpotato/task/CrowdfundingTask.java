package cn.smallpotato.task;

import cn.smallpotato.webmagic.pipeline.CrowdfundingPipeline;
import cn.smallpotato.webmagic.processor.CrowdfundingPageProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

/**
 * TODO 引入quartz，更灵活
 * 爬虫定时调度
 * @author panjb
 */
@Component
public class CrowdfundingTask {

    private final CrowdfundingPipeline crowdfundingPipeline;

    public CrowdfundingTask(CrowdfundingPipeline crowdfundingPipeline) {
        this.crowdfundingPipeline = crowdfundingPipeline;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 3600 * 1000)
    public void execute() {
        Spider.create(new CrowdfundingPageProcessor())
                .addUrl("https://zhongchou.modian.com/publishing/top_money/success")
                .addPipeline(this.crowdfundingPipeline)
                .thread(5)
                .run();
    }
}
