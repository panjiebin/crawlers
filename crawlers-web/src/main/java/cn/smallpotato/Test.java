package cn.smallpotato;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author panjb
 */
public class Test {

    public static void main(String[] args) throws IOException {
        Map<String, Integer> tmp = get();
        File file = new File("D:\\data");
        File[] files = file.listFiles();
        BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\NATO.csv", true));
        for (File file1 : files) {
            BufferedReader reader = new BufferedReader(new FileReader(file1));
            String name = file1.getName().substring(0, file1.getName().indexOf(".txt"));
            if (tmp.containsKey(name)) {
                name = name + "_" + (tmp.get(name) + 1);
            }
            BufferedWriter writer2 = new BufferedWriter(new FileWriter("D:\\data3\\" + name + ".txt"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    line = line.trim().replace("&lt;", "");
                    line = line.replace("&nbsp;", "");
                    line = line.replace("p&gt;", "");
                    line = line.replace("|", " ");
                    sb.append(line);
                    writer2.write(line);
                    writer2.newLine();
                }
            }
            writer2.flush();
            writer2.close();
            String fileName = file1.getName().substring(0, file1.getName().indexOf(".txt"));
            writer.write(fileName+ "|" + sb);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    private static Map<String, Integer> get() {
        File file = new File("D:\\data3");
        File[] files = file.listFiles();
        Map<String, Integer> c = new HashMap<>();
        for (File file1 : files) {
            String fileName = file1.getName();
            if (fileName.contains("_")) {
                fileName = fileName.substring(0, fileName.indexOf("_"));
                c.put(fileName, c.get(fileName) + 1);
            } else {
                fileName = fileName.substring(0, fileName.indexOf(".txt"));
                c.put(fileName, 0);
            }
        }
        return c;
    }
}
