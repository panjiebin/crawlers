package cn.smallpotato.cases;

import cn.smallpotato.common.http.HttpHelper;
import cn.smallpotato.utils.WebDriverUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author panjb
 */
public class MilitaryExpenditureCrawler {

    public static void main(String[] args) throws IOException {
        String url = "https://digitallibrary.un.org/search?ln=zh_CN&cc=Speeches&p=&f=&rm=&ln=zh_CN&sf=&so=d&rg=50&c=Speeches&c=&of=hb&fti=0&fct__8=DISARMAMENT--GENERAL%20AND%20COMPLETE&fct__7=United%20States&fti=0";
//        String html = HttpHelper.doGet(url, String.class);
//        Document document = Jsoup.parse(html);
//        Element searchForm = document.select("body > div.pagebody > div.pagebodystripemiddle > form").get(2);
//        Elements rows = searchForm.select("table > tbody > tr");
//        RemoteWebDriver driver = WebDriverUtils.createChromeDriver("C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
//        driver.get(url);
//        List<WebElement> elements = driver.findElements(new By.ByCssSelector("body > div.pagebody > div.pagebodystripemiddle > form"));
        String fileUrl = "https://digitallibrary.un.org/record/3827885/files/A_73_PV.104-EN.pdf?ln=zh_CN";
        CloseableHttpClient client = HttpHelper.getClient();
        HttpGet httpGet = new HttpGet(fileUrl);
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
    }


}
