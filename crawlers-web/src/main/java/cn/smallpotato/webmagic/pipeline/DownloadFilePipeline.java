package cn.smallpotato.webmagic.pipeline;

import cn.smallpotato.common.http.HttpHelper;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Optional;

/**
 * @author panjb
 */
public class DownloadFilePipeline implements Pipeline {

    private final static Logger logger = LoggerFactory.getLogger(DownloadFilePipeline.class);

    private int cnt = 0;

    @Override
    public void process(ResultItems resultItems, Task task) {
        FileMeta item = resultItems.get("item");
        Optional.ofNullable(item).ifPresent(fileMeta -> {
            HttpHelper.downloadFile(fileMeta.getFileUrl(), fileMeta.getDiskPath());
            cnt++;
            logger.info("文件[{}]，下载成功！", fileMeta.getDiskPath());
            if (cnt % 50 == 0) {
                logger.info("已下载文件[{}]", cnt);
            }
        });
        this.waitMoment();
    }

    private void waitMoment() {
        try {
            Thread.sleep(RandomUtils.nextInt(1000, 3000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
