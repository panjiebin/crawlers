package cn.smallpotato.cases;

import cn.smallpotato.common.http.HttpHelper;
import cn.smallpotato.common.model.*;
import cn.smallpotato.entity.Book;
import cn.smallpotato.service.BookService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.util.Maps;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author panjb
 */
public class DangDangCrawler extends AbstractCrawler<String, Book> {

    private final BookService bookService;
    private final Set<String> urls;

    public DangDangCrawler(BookService bookService,Set<String> urls) {
        this.bookService = bookService;
        this.urls = urls;
    }

    @Override
    protected BlockingQueue<String> getCrawlerTasks() {
        List<String> urls = new ArrayList<>();
        for (int i = 45; i < 55; i++) {
            String url = "http://category.dangdang.com/pg" + i + "-cp01.27.01.13.00.00.html";
            String html = HttpHelper.doGet(url, new TypeReference<String>() {});
            Document document = Jsoup.parse(html);
            Elements elements = document.select("#component_59 > li");
            for (org.jsoup.nodes.Element element : elements) {
                String productUrl = element.select("a").attr("href");
                String fullUrl = "http:" + productUrl;
                if (!this.urls.contains(fullUrl)) {
                    urls.add(fullUrl);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new LinkedBlockingQueue<>(urls);
    }

    @Override
    protected Writer<Book> createWriter(BlockingQueue<Element> queue) {
        return new BookWriter(queue, bookService);
    }

    @Override
    protected Downloader<String, Book> createCrawler(BlockingQueue<String> taskQueue, BlockingQueue<Element> elementQueue, CountDownLatch countDownLatch) {
        return new BookDownloader(taskQueue, elementQueue, countDownLatch);
    }

    @Override
    protected int crawlerSize() {
        return 3;
    }

    private static class BookWriter extends AbstractWriter<Book> {

        private final BookService bookService;

        public BookWriter(BlockingQueue<Element> queue, BookService bookService) {
            super(queue);
            this.bookService = bookService;
        }

        @Override
        public void write(Book ele) {
            Book book = bookService.queryByUrl(ele.getUrl());
            if (book == null) {
                bookService.save(ele);
            }
        }

        @Override
        public void close() {

        }
    }

    private static class BookDownloader extends AbstractDownloader<String, Book> {

        private final String[] userAgents = new String[]{"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; AcooBrowser; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Acoo Browser; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506)",
                "Mozilla/4.0 (compatible; MSIE 7.0; AOL 9.5; AOLBuild 4337.35; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",
                "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)",
                "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 2.0.50727; Media Center PC 6.0)",
                "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 1.0.3705; .NET CLR 1.1.4322)",
                "Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2; .NET CLR 3.0.04506.30)",
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN) AppleWebKit/523.15 (KHTML, like Gecko, Safari/419.3) Arora/0.3 (Change: 287 c9dfb30)",
                "Mozilla/5.0 (X11; U; Linux; en-US) AppleWebKit/527+ (KHTML, like Gecko, Safari/419.3) Arora/0.6",
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.2pre) Gecko/20070215 K-Ninja/2.1.1",
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9) Gecko/20080705 Firefox/3.0 Kapiko/3.0",
                "Mozilla/5.0 (X11; Linux i686; U;) Gecko/20070322 Kazehakase/0.4.5",
                "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko Fedora/1.9.0.8-1.fc10 Kazehakase/0.5.6",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.20 (KHTML, like Gecko) Chrome/19.0.1036.7 Safari/535.20",
                "Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; fr) Presto/2.9.168 Version/11.52",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.11 TaoBrowser/2.0 Safari/536.11",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.71 Safari/537.1 LBBROWSER",
                "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; LBBROWSER)",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E; LBBROWSER)",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.84 Safari/535.11 LBBROWSER"

        };

        private final Map<String, String> headers = Maps.newHashMap("Cookie", "secret_key=6392806543d92b4f893a809ea2586a2e; __permanent_id=20220930204136380371151806927601726; __visit_id=20220930204136416527965902182584020; __out_refer=; dangdang.com=email=MTgwNjA0Nzk5MzEzNjU4N0BkZG1vYmlscGhvbmVfX3VzZXIuY29t&nickname=&display_id=7757670961225&customerid=rSW8sHERH/68K+XbdCLqOw==&viptype=ZrZcy+1K2qk=&show_name=180****9931; dest_area=country_id=9000&province_id=111&city_id =0&district_id=0&town_id=0; ddscreen=2; __rpm=|login_page...1664545903826; sessionID=pc_f6121837eaf1d8b1b4e0b237527d3410c3b29ab9321bbee699ac162bc3d17df4; USERNUM=JmvugUJFV2kaPHG5fvn8lg==; login.dangdang.com=.ASPXAUTH=qxm8p6oLazcwFMRah0LGeXr1cC5dVgKWxd/6ii6tz4VbSmOFwsLrFA==; ddoy=email=1806047993136587@ddmobilphone__user.com&nickname=&validatedflag=0&uname=&utype=1&.ALFG=off&.ALTM=1664545904325; LOGIN_TIME=1664546390261; __trace_id=20220930215954370446095434917910299; pos_6_start=1664546394415; pos_6_end=1664546394555");

        public BookDownloader(BlockingQueue<String> taskQueue, BlockingQueue<Element> elementQueue, CountDownLatch countDownLatch) {
            super(taskQueue, elementQueue, countDownLatch);
        }

        @Override
        public Iterable<Book> download(String task) {
            try {
                Thread.sleep(RandomUtils.nextInt(3000, 5000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            headers.put("User-Agent", userAgents[RandomUtils.nextInt(0, userAgents.length)]);
            String html = HttpHelper.doGet(task, headers, new TypeReference<String>() {});
            Document document = Jsoup.parse(html);
            String name = document.select("#product_info > div.name_info > h1").text();
            String authorHtml = document.select("#author").toString();
            if (authorHtml.indexOf("著") != -1) {
                authorHtml = authorHtml.substring(0, authorHtml.indexOf("著") + 1) + "</span>";
            }
            Document d2 = Jsoup.parse(authorHtml);
            Elements a = d2.select("a");
            StringJoiner author = new StringJoiner(",");
            for (org.jsoup.nodes.Element element : a) {
                author.add(element.text());
            }
            String publish = document.select("#product_info > div.messbox_info > span:nth-child(2) > a").text();
            String number =  document.select("#detail_describe > ul > li:nth-child(5)").text();
            String bookNumber = number.substring(number.indexOf("：") + 1);
            Book book = new Book(name.trim(), author.toString(), publish, bookNumber, task);
            return Collections.singleton(book);
        }


    }
}
