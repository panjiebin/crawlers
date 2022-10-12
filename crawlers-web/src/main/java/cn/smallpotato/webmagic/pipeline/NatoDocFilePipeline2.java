package cn.smallpotato.webmagic.pipeline;

import cn.smallpotato.entity.NatoDoc;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author panjb
 */
public class NatoDocFilePipeline2 implements Pipeline {

    private int cnt = 0;

    public NatoDocFilePipeline2() {
    }


    @Override
    public void process(ResultItems resultItems, Task task) {
        String item = resultItems.get("item");
        if (item != null) {
            this.doWrite(item);
        }
    }

    private void doWrite(String content) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("D:\\pdf.txt", true));
            writer.write(content);
            writer.newLine();
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
