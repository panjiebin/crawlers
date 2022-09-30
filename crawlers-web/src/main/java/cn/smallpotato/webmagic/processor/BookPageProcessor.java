package cn.smallpotato.webmagic.processor;

import cn.smallpotato.entity.Book;
import org.apache.commons.lang3.RandomUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.CssSelector;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;
import java.util.StringJoiner;

/**
 * @author panjb
 */
public class BookPageProcessor implements PageProcessor {
    private final Site site = Site.me()
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")
            .addHeader("Cookie", "__permanent_id=20210702160234515282835219982356489; ddscreen=2; __visit_id=20220930151950071628620148733239661; __out_refer=; dest_area=country_id=9000&province_id=111&city_id=0&district_id=0&town_id=0; sessionID=pc_6391461006a695a4dd8d293e4b3df0b4372e367c89c3c1cd1a7f6b5e1dfa1953; USERNUM=JmvugUJFV2kaPHG5fvn8lg==; login.dangdang.com=.ASPXAUTH=qxm8p6oLazcwFMRah0LGeXr1cC5dVgKWxd/6ii6tz4VbSmOFwsLrFA==; dangdang.com=email=MTgwNjA0Nzk5MzEzNjU4N0BkZG1vYmlscGhvbmVfX3VzZXIuY29t&nickname=&display_id=7757670961225&customerid=rSW8sHERH/68K+XbdCLqOw==&viptype=ZrZcy+1K2qk=&show_name=180****9931; ddoy=email=1806047993136587@ddmobilphone__user.com&nickname=&validatedflag=0&uname=&utype=1&.ALFG=off&.ALTM=1664525924078; __rpm=|s_605253.451680112839,451680112840.4.1664526215710; pos_9_end=1664526271745; pos_6_start=1664526271888; ad_ids=7564154,10395115,2754971,2533495,2533482,14129493,6241367,5066933|#3,3,3,3,3,3,2,1; LOGIN_TIME=1664527841348; pos_6_end=1664527876124; search_passback=60ae4da5c500700d2bae366300000000e45f640002ae3663; __trace_id=20220930165157266208159447362965532");

    private int count = 1;

    @Override
    public void process(Page page) {
        //http://product.dangdang.com/25580397.html
        //http://category.dangdang.com/cp01.27.01.13.00.00.html
        if (page.getUrl().toString().startsWith("http://category.dangdang.com")) {
            List<Selectable> list = page.getHtml().css("#component_59 > li").nodes();
            for (Selectable selectable : list) {
                Selectable productUrl = selectable.css("a", "href");
                page.addTargetRequest("http:" + productUrl);
            }
            count++;
            if (count <= 30) {
                page.addTargetRequest("http://category.dangdang.com/pg" + count + "-cp01.27.01.13.00.00.html");
            }
        } else {
            Book book = this.parseBook(page);
            page.putField("item", book);
            try {
                Thread.sleep(RandomUtils.nextInt(1000, 5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    private Book parseBook(Page page) {
        String name = page.getHtml().css("#product_info > div.name_info > h1", "text").toString();
        String authorHtml = page.getHtml().css("#author").toString();
        if (authorHtml.indexOf("著") != -1) {
            authorHtml = authorHtml.substring(0, authorHtml.indexOf("著") + 1) + "</span>";
        }
        Document document = Jsoup.parse(authorHtml);
        Elements a = document.select("a");
        StringJoiner author = new StringJoiner(",");
        for (Element element : a) {
            author.add(element.text());
        }
        String publish = page.getHtml().css("#product_info > div.messbox_info > span:nth-child(2) > a", "text").toString();
        String number = page.getHtml().css("#detail_describe > ul > li:nth-child(5)", "text").toString();
        String bookNumber = number.substring(number.indexOf("：") + 1);
        return new Book(name.trim(), author.toString(), publish, bookNumber, page.getUrl().toString());
    }

    public static void main(String[] args) {
        Spider.create(new BookPageProcessor())
                .addUrl("http://product.dangdang.com/29170056.html")
                .run();
    }
}
