package cn.smallpotato.webmagic.processor;

import cn.smallpotato.entity.NatoDoc;
import org.apache.commons.lang3.RandomUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

/**
 * @author panjb
 */
public class NatoDocPageProcessor implements PageProcessor {

    private final Site site = Site.me();

    private String detailUrlPrefix = "https://www.nato.int/cps/en/natohq/";
    private String listUrlPrefix = "https://www.nato.int/cps/en/natohq/official_texts.htm?search_types=Official%20text&display_mode=official_text&date_from=01.01.1950&date_to=31.12.2013&keywordquery=*&chunk=";

    private int count = 55;
    private int total = 0;

    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            if (page.getUrl().toString().startsWith(listUrlPrefix)) {
                Selectable resultHtml = page.getHtml().css("body > div.page > div > article > div > div.searchresults");
                List<Selectable> rows = resultHtml.css("table.table.table-news > tbody > tr").nodes();
                for (Selectable row : rows) {
                    String uri = row.css("td:nth-child(2) > p > a", "href").toString();
                    if (uri.endsWith(".pdf")) {
                        String date = row.css("td", "text").toString().trim();
                        total++;
                        page.putField("item", date + "=" + uri);
                        System.out.println("total = " + total);
                    } else {
//                        page.addTargetRequest(detailUrlPrefix + uri);
                    }
                }
                count++;
                if (count <= 84) {
                    page.addTargetRequest(listUrlPrefix + count);
                }
                Thread.sleep(RandomUtils.nextInt(1000, 3000));
            } else {
                NatoDoc doc = parseDoc(page);
                page.putField("item", doc);
                Thread.sleep(RandomUtils.nextInt(1000, 3000));
            }
        } catch (InterruptedException e) {
            System.out.println("fail url = " + url);
        }

    }

    private NatoDoc parseDoc(Page page) {
        String dateStr = page.getHtml().css("body > div.page > div > article > div > div:nth-child(1) > div > ul.list-meta > li:nth-child(1)", "text").toString();
        String date = null;
        if (dateStr.contains("-")) {
            date = dateStr.substring(0, dateStr.indexOf("-") - 1).trim();
        } else {
            date = dateStr.trim();
        }
        String title = page.getHtml().css("body > div.page > div > article > div > div:nth-child(1) > div > h1", "text").toString();
        String content = page.getHtml().css("body > div.page > div > article > div > div:nth-child(1) > div > section").toString();
        String htmlRegex = "<[^>]+>";
        content = content.replaceAll(htmlRegex, "");
        return new NatoDoc(title, date, content, page.getUrl().toString());
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    public static void main(String[] args) {
        Spider.create(new NatoDocPageProcessor())
//                .addUrl("https://www.nato.int/cps/en/natohq/official_texts_26621.htm?selectedLocale=en")
//                .addPipeline(new NatoDocFilePipeline(cache))
//                .addUrl("https://www.nato.int/cps/en/natohq/news_105470.htm?selectedLocale=en")
                .addUrl("https://www.nato.int/cps/en/natohq/official_texts.htm?search_types=Official%20text&display_mode=official_text&date_from=01.01.1950&date_to=31.12.2013&keywordquery=*&chunk=55")
                .thread(5)
                .start();

    }
}
