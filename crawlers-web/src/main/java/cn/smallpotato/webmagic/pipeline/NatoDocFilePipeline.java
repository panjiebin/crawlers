package cn.smallpotato.webmagic.pipeline;

import cn.smallpotato.entity.NatoDoc;
import org.apache.tomcat.util.net.WriteBuffer;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author panjb
 */
public class NatoDocFilePipeline implements Pipeline {

    private Map<String, Integer> cache = new HashMap<>();
    private int cnt = 0;

    public NatoDocFilePipeline() {
    }

    public NatoDocFilePipeline(Map<String, Integer> cache) {
        this.cache = cache;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        NatoDoc item = resultItems.get("item");
        if (item != null) {
            String fileName = item.getDate();
            if (cache.containsKey(item.getDate())) {
                int index = cache.get(item.getDate());
                index++;
                fileName = fileName + "_" + index;
                cache.put(item.getDate(), index);
            } else {
                cache.put(item.getDate(), 0);
            }
            this.doWrite(item.getContent(), fileName);
        }
    }

    private void doWrite(String content, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("D:\\data\\" + fileName + ".txt"));
            writer.write(content);
            cnt++;
            if (cnt % 100 == 0) {
                System.out.println("cnt = " + cnt);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
