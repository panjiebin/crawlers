package cn.smallpotato.webmagic.pipeline;


import cn.smallpotato.entity.Crowdfunding;
import cn.smallpotato.service.CrowdfundingService;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * @author panjb
 */
@Component
public class CrowdfundingPipeline implements Pipeline {

    private final CrowdfundingService crowdfundingService;

    public CrowdfundingPipeline(CrowdfundingService crowdfundingService) {
        this.crowdfundingService = crowdfundingService;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Crowdfunding item = resultItems.get("item");
        if (item != null) {
            Crowdfunding old = this.crowdfundingService.queryByUrl(item.getUrl());
            if (old == null) {
                this.crowdfundingService.save(item);
            }
        }
    }
}
