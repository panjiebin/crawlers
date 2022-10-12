package cn.smallpotato.webmagic.pipeline;

import cn.smallpotato.entity.NatoDoc;
import cn.smallpotato.service.NatoDocService;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * @author panjb
 */
@Component
public class NatoDocPipeline implements Pipeline {
    private final NatoDocService natoDocService;
    public NatoDocPipeline(NatoDocService natoDocService) {
        this.natoDocService = natoDocService;
    }
    @Override
    public void process(ResultItems resultItems, Task task) {
        NatoDoc item = resultItems.get("item");
        if (item != null) {
            NatoDoc old = this.natoDocService.queryByUrl(item.getUrl());
            if (old == null) {
                this.natoDocService.save(item);
            }
        }
    }
}
