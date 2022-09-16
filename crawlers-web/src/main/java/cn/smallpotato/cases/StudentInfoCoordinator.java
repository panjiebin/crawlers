package cn.smallpotato.cases;

import cn.smallpotato.common.model.*;
import cn.smallpotato.output.TextFileSink;
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
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author panjb
 */
public class StudentInfoCoordinator extends Coordinator<String, StudentInfoCoordinator.StudentInfo> {

    private final static Logger logger = LoggerFactory.getLogger(StudentInfoCoordinator.class);

    private final String url = "http://crp.hbgt.com.cn/oa/login.aspx";

    public static void main(String[] args) {
        new StudentInfoCoordinator().start();
    }

    @Override
    protected BlockingQueue<String> getCrawlerTasks() {
        WebDriver driver = this.initDriver();
        login(driver, url);
        Select select = new Select(driver.findElement(new By.ByCssSelector("#DropDownList_班别")));
        List<WebElement> options = select.getOptions();
        BlockingQueue<String> classNameQueue = options.stream()
                .map(WebElement::getText).filter(val -> val.contains("2021"))
                .distinct()
                .sorted()
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        logger.info("需要爬取的班级总数[{}]", classNameQueue.size());
        driver.quit();
        return classNameQueue;
    }

    @Override
    protected Writer<StudentInfo> createWriter(BlockingQueue<Element> queue) {
        return new FileWriter<>(queue,new TextFileSink<>("D:\\student.csv"));
    }

    @Override
    protected cn.smallpotato.common.model.Crawler createCrawler(BlockingQueue<String> taskQueue, BlockingQueue<Element> elementQueue, CountDownLatch countDownLatch) {
        return new StudentCrawler(taskQueue, elementQueue, countDownLatch, url);
    }

    private void login(WebDriver driver, String url) {
        try {
            this.doLogin(driver, url);
            this.switchToSearchPage(driver);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doLogin(WebDriver driver, String url) throws InterruptedException {
        driver.get(url);
        String card = "";
        String password = "";
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

    class StudentCrawler extends AbstractWithInputCrawler<String, StudentInfo> {

        private final WebDriver driver;

        public StudentCrawler(BlockingQueue<String> taskQueue, BlockingQueue<Element> elementQueue, CountDownLatch countDownLatch, String loginUrl) {
            super(taskQueue, elementQueue, countDownLatch);
            this.driver = initDriver();
            login(this.driver, loginUrl);
        }

        @Override
        public Iterable<StudentInfo> crawling(String className) {
            List<StudentInfo> infos = new ArrayList<>();
            try {
                driver.findElement(new By.ByXPath("//*[@id=\"DropDownList_班别\"]/option[@value=\"" + className + "\"]")).click();
                Thread.sleep(1000);
                processTable(infos);
                switchWindowOne();
                driver.findElement(new By.ByCssSelector("#lin_b_下一页")).click();
                Thread.sleep(2000);
                processTable(infos);
                logger.info("班级[{}]学生数据[{}]写入输出队列", className, infos.size());
                switchWindowOne();
            } catch (Exception e) {
                logger.warn("班级[{}]爬取失败，重新添加到任务队列", className);
                try {
                    taskQueue.put(className);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            return infos;
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

    static class StudentInfo implements Element {

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
