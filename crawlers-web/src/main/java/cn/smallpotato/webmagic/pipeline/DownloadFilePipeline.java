package cn.smallpotato.webmagic.pipeline;

import cn.smallpotato.common.http.HttpHelper;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
            CloseableHttpClient client = HttpHelper.getClient();
            HttpGet httpGet = new HttpGet(fileMeta.getFileUrl());
            FileOutputStream os = null;
            InputStream is = null;
            try {
                CloseableHttpResponse response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                File file = new File(fileMeta.getDiskPath());
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                os = new FileOutputStream(file);
                is = entity.getContent();
                byte[] buffer = new byte[1024];
                int ch;
                while ((ch = is.read(buffer)) != -1) {
                    os.write(buffer, 0 ,ch);
                }
                cnt++;
                logger.info("文件[{}]，下载成功！", fileMeta.getDiskPath());
                if (cnt % 50 == 0) {
                    logger.info("已下载文件[{}]", cnt);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.flush();
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
