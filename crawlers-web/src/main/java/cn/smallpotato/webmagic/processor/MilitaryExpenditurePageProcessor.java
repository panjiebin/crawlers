package cn.smallpotato.webmagic.processor;

import cn.smallpotato.common.http.HttpHelper;
import cn.smallpotato.webmagic.pipeline.DownloadFilePipeline;
import cn.smallpotato.webmagic.pipeline.FileMeta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.RandomUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author panjb
 */
public class MilitaryExpenditurePageProcessor implements PageProcessor {

    private static final String LIST_PAGE = "listPage";
    private static final String DETAIL_PAGE = "detailPage";
    private final Site site = Site.me().setTimeOut(2 * 60 * 1000);

    @Override
    public void process(Page page) {
        Request currRequest = page.getRequest();
        String type = currRequest.getExtra("type");
        if (LIST_PAGE.equals(type)) {
            List<Selectable> list = page.getHtml().css("body > div.pagebody > div.pagebodystripemiddle > form[action=/yourbaskets/add] > table > tbody > tr").nodes();
            for (Selectable tr : list) {
                String uri = tr.css("td:nth-child(2) > div.moreinfo > span:nth-child(1) > a", "href").toString();
                String url = "https://digitallibrary.un.org/" + uri;
                Request targetRequest = new Request(url);
                targetRequest.putExtra("type", DETAIL_PAGE);
                targetRequest.putExtra("country", currRequest.getExtra("country"));
                page.addTargetRequest(targetRequest);
            }
            this.waitMoment();
        } else if (DETAIL_PAGE.equals(type)) {
            String meetUrl = page.getHtml().css("#details-collapse > div:nth-child(5) > span.value.col-xs-12.col-sm-9.col-md-10 > a", "href").toString();
            String id = meetUrl.substring(meetUrl.lastIndexOf("/") + 1, meetUrl.indexOf("?"));
            String url = "https://digitallibrary.un.org/api/v1/file?recid=" + id;
            List<PdfFileMeta> metas = HttpHelper.doGet(url, new TypeReference<List<PdfFileMeta>>() {});
            metas.stream()
                    .filter(meta -> "English".equals(meta.description))
                    .findFirst()
                    .ifPresent(meta -> {
                        String fileName = meta.getName() + meta.format;
                        FileMeta fileMeta = new FileMeta(meta.getUrl(), buildDiskPath(currRequest.getExtra("country"), fileName));
                        page.putField("item", fileMeta);
                    });
            this.waitMoment();
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        List<Request> requests = getAllCountries();
        Request[] arr = new Request[requests.size()];
        Spider.create(new MilitaryExpenditurePageProcessor())
                .addRequest(requests.toArray(arr))
                .addPipeline(new DownloadFilePipeline())
                .thread(5)
                .start();
    }

    private void waitMoment() {
        try {
            Thread.sleep(RandomUtils.nextInt(1000, 3000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildDiskPath(String country, String fileName) {
        return "D:\\test\\" + country + "\\" + fileName;
    }

    private static List<Request> getAllCountries() {
        BufferedReader reader = null;
        try {
            List<Request> requests = new ArrayList<>();
            InputStream is = MilitaryExpenditurePageProcessor.class.getClassLoader().getResourceAsStream("url.txt");
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] s = line.split("\\|");
                if (s.length >= 3) {
                    requests.add(createRequest(s[0], s[2], LIST_PAGE));
                    int pageSize = Integer.parseInt(s[1]);
                    if (pageSize > 1) {
                        for (int i = 1; i < pageSize; i++) {
                            String url = s[2].substring(0, s[2].length() - 1) + ((i-1) * 100 + 1);
                            requests.add(createRequest(s[0], url, LIST_PAGE));

                        }
                    }
                }
            }
            return requests;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Request createRequest(String country, String url, String type) {
        Request request = new Request(url);
        request.putExtra("country", country);
        request.putExtra("type", type);
        return request;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PdfFileMeta {
        private String name;
        private String url;
        private String description;
        private String format;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }
    }
}
