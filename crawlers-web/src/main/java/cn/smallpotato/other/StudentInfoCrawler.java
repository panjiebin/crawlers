package cn.smallpotato.other;

import cn.smallpotato.output.FileSink;
import cn.smallpotato.output.Sink;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author panjb
 */
public class StudentInfoCrawler {

    private final static Logger logger = LoggerFactory.getLogger(StudentInfoCrawler.class);

    private final ExecutorService executor = new ThreadPoolExecutor(5,
            10,
            10,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("crawler-thread-%d").build());

    public static void main(String[] args) {
        StudentInfoCrawler crawler = new StudentInfoCrawler();
        crawler.crawling();
    }

    public void crawling() {
        String url = "http://crp.hbgt.com.cn/oa/login.aspx";
        try {
            RemoteWebDriver driver = this.initDriver();
            this.login(driver, url);
            this.switchToSearchPage(driver);
            Select select = new Select(driver.findElement(new By.ByCssSelector("#DropDownList_班别")));
            List<WebElement> options = select.getOptions();
            ConcurrentLinkedQueue<String> classNameQueue = options.stream()
                    .map(WebElement::getText).filter(val -> val.contains("2021"))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
            logger.info("需要爬取的班级总数[{}]", classNameQueue.size());
            driver.quit();
            int threads = 5;
            ConcurrentLinkedQueue<StudentInfo> queue = new ConcurrentLinkedQueue<>();
            CountDownLatch countDownLatch = new CountDownLatch(threads);
            CountDownLatch writeLatch = new CountDownLatch(1);
            for (int i = 0; i < threads; i++) {
                executor.execute(new StudentInfoDownloader(classNameQueue, queue, countDownLatch, url));
            }
            StudentInfoWriter writer = new StudentInfoWriter(queue, writeLatch, "D:\\student.txt");
            executor.execute(writer);
            countDownLatch.await();
            writer.setEnd(true);
            writeLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("学生信息爬取完毕");
    }

    private void login(WebDriver driver, String url) throws InterruptedException {
        driver.get(url);
        String card = "90309253";
        String password = "19941217";
        driver.findElement(new By.ByCssSelector("#txt_卡号")).sendKeys(card);
        driver.findElement(new By.ByCssSelector("#txt_密码")).sendKeys(password);
        driver.findElement(new By.ByCssSelector("#Button_登陆")).click();
        Thread.sleep(1000);
    }

    private void switchToSearchPage(WebDriver driver) throws InterruptedException {
        driver.get("http://crp.hbgt.com.cn/oa/ilogin/klogin.aspx?link=http://jw.hbgt.com.cn:8081/st/login_io.aspx?id=1&U=http://jw.hbgt.com.cn:8081/st/sys/search/st_search_a.aspx");
        Thread.sleep(1000);
    }

    private RemoteWebDriver initDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--window-size=3120,2080");
        return new ChromeDriver(chromeOptions);
    }

    static class StudentInfoWriter implements Runnable {

        private final ConcurrentLinkedQueue<StudentInfo> queue;
        private final CountDownLatch countDownLatch;
        private final Sink<StudentInfo> sink;
        private volatile boolean isEnd;

        public StudentInfoWriter(ConcurrentLinkedQueue<StudentInfo> queue, CountDownLatch countDownLatch, String filePath) {
            this.queue = queue;
            this.countDownLatch = countDownLatch;
            this.sink = new FileSink<>(filePath);
            sink.init();
        }

        @Override
        public void run() {
            try {
                while (!isEnd || !queue.isEmpty()) {
                    Optional.ofNullable(queue.poll()).ifPresent(sink::process);
                }
                logger.info("学生信息写入完成！");
            } finally {
                this.sink.close();
                countDownLatch.countDown();
            }
        }

        public void setEnd(boolean end) {
            isEnd = end;
        }
    }

    class StudentInfoDownloader implements Runnable {

        private final ConcurrentLinkedQueue<String> classNameQueue;
        private final ConcurrentLinkedQueue<StudentInfo> queue;
        private final CountDownLatch countDownLatch;
        private final WebDriver driver;
        private final String loginUrl;

        public StudentInfoDownloader(ConcurrentLinkedQueue<String> classNameQueue,
                                     ConcurrentLinkedQueue<StudentInfo> queue,
                                     CountDownLatch countDownLatch,
                                     String url) {
            this.classNameQueue = classNameQueue;
            this.queue = queue;
            this.countDownLatch = countDownLatch;
            this.driver = initDriver();
            this.loginUrl = url;
        }

        @Override
        public void run() {
            try {
                login(driver, loginUrl);
                switchToSearchPage(driver);
                while (!classNameQueue.isEmpty()) {
                    String className = classNameQueue.poll();
                    logger.info("开始爬取班级[{}], 剩余[{}]", className, classNameQueue.size());
                    try {
                        driver.findElement(new By.ByXPath("//*[@id=\"DropDownList_班别\"]/option[@value=\"" + className + "\"]")).click();
                        Thread.sleep(1000);
                        List<StudentInfo> infos = new ArrayList<>();
                        processTable(infos);
                        switchWindowOne();
                        driver.findElement(new By.ByCssSelector("#lin_b_下一页")).click();
                        Thread.sleep(2000);
                        processTable(infos);
                        queue.addAll(infos);
                        logger.info("班级[{}]学生数据写入输出队列", className);
                        switchWindowOne();
                    } catch (Exception e) {
                        logger.warn("班级[{}]爬取失败，重新添加到任务队列", className);
                        classNameQueue.offer(className);
                    }
                }
            } catch (InterruptedException e) {
                logger.error("登录查询页失败", e);
            } finally {
                countDownLatch.countDown();
            }
        }

        private void switchWindowOne() {
            driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(0));
        }

        private void switchWindowTwo() throws InterruptedException {
            Set<String> windowHandles = driver.getWindowHandles();
            if (windowHandles.size() < 2) {
                Thread.sleep(2000);
            }
            driver.switchTo().window(new ArrayList<>(driver.getWindowHandles()).get(1));
        }

        public void processTable(List<StudentInfo> infos) throws InterruptedException {
            WebElement table = driver.findElement(new By.ByXPath("//*[@id=\"Grd_学生\"]"));
            List<WebElement> rows = table.findElements(new By.ByTagName("tr"));
            for (int i = 1; i < rows.size(); i++) {
                extractStudentInfo(rows, infos, i);
                table = driver.findElement(new By.ByXPath("//*[@id=\"Grd_学生\"]"));
                rows = table.findElements(new By.ByTagName("tr"));
            }

        }

        private void extractStudentInfo(List<WebElement> rows, List<StudentInfo> infos, int i) throws InterruptedException {
            rows.get(i).findElements(new By.ByTagName("td")).get(1).findElement(new By.ByTagName("a")).click();
            Thread.sleep(3000);
            driver.findElement(new By.ByCssSelector("#LinkButton_学生基本情况")).click();
            switchWindowTwo();
            Thread.sleep(1000);
            String name = driver.findElement(new By.ByCssSelector("#txt_姓名")).getAttribute("value");
            String phone = driver.findElement(new By.ByCssSelector("#txt_学生手机")).getAttribute("value");
            String className = driver.findElement(new By.ByCssSelector("#txt_行政班")).getAttribute("value");
            StudentInfo studentInfo = new StudentInfo(name, phone, className);
            infos.add(studentInfo);
            driver.close();
            switchWindowOne();
        }
    }

    static class StudentInfo {

        private String name;
        private String phone;
        private String className;
        public StudentInfo(String name, String phone, String className) {
            this.name = name;
            this.phone = phone;
            this.className = className;
        }
    }
}
