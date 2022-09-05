package cn.smallpotato.webmagic.processor;

import cn.smallpotato.entity.Crowdfunding;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

/**
 * @author panjb
 */
public class CrowdfundingPageProcessor implements PageProcessor {

    private final Site site = Site.me();

    private int count = 1;

    @Override
    public void process(Page page) {
        try {
            List<Selectable> nodes = page.getHtml().css("div.pro_field > ul > li").nodes();
            if (nodes == null || nodes.isEmpty()) {
                Crowdfunding crowdfunding = parseDetailPage(page);
                page.putField("item", crowdfunding);
                Thread.sleep(RandomUtils.nextInt(1000, 3000));
            } else {
                for (Selectable node : nodes) {
                    String detailUrl = node.css("a", "href").toString();
                    page.addTargetRequest(detailUrl);
                }
                count++;
                if (count > 49) {
                    return;
                }
                page.addTargetRequest("https://zhongchou.modian.com/publishing/top_money/success/" + count);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Crowdfunding parseDetailPage(Page page) {
        Crowdfunding crowdfunding = new Crowdfunding();
        crowdfunding.setUrl(page.getUrl().toString());
        crowdfunding.setName(page.getHtml().css("div.short-cut > h3 > span", "text").toString().trim());
        Selectable center = page.getHtml().css("div.center > div.center-top > div.col1.project-goal.success");
        String goalStr = center.css("span.goal-money", "text").toString();
        if (StringUtils.isNotBlank(goalStr)) {
            // 众筹完成
            String goalMoney = goalStr.substring(goalStr.indexOf("¥") + 1).replace(",", "");
            crowdfunding.setGoal(Double.parseDouble(goalMoney));
            crowdfunding.setPercentage(center.css("span.percent", "text").toString());
            String completed = page.getHtml().css("div.center > div.center-top > div.col1.project-goal.success > h3 > span", "text").toString();
            crowdfunding.setCompleted(Double.parseDouble(completed.replace(",", "")));
        } else {
            Selectable center2 = page.getHtml().css("div.center > div.center-top > div.col1.project-goal.abort.fail");
            String str2 = center2.css("p.txt.clearfloat > span.goal-money", "text").toString();
            if (StringUtils.isNotBlank(str2)) {
                // 众筹终止
                crowdfunding.setGoal(Double.parseDouble(str2.substring(str2.indexOf("¥") + 1).replace(",", "")));
                crowdfunding.setPercentage(center2.css("p.txt.clearfloat > span.percent", "text").toString());
                String completed = center2.css("h3 > span", "text").toString();
                crowdfunding.setCompleted(Double.parseDouble(completed.replace(",", "")));
            } else {
                // 众筹取消
                Selectable center3 = page.getHtml().css("div.center > div.center-top > div.col1.project-goal.cancel.fail");
                String str3 = center3.css("p.txt.clearfloat > span.goal-money", "text").toString();
                crowdfunding.setGoal(Double.parseDouble(str3.substring(str3.indexOf("¥") + 1).replace(",", "")));
                crowdfunding.setPercentage(center3.css("p.txt.clearfloat > span.percent", "text").toString());
                String completed = center3.css("h3 > span", "text").toString();
                crowdfunding.setCompleted(Double.parseDouble(completed.replace(",", "")));
            }
        }
        crowdfunding.setPeople(Integer.parseInt(page.getHtml().css("div.center > div.center-top > div.col3.support-people > h3 > span", "text").toString()));
        crowdfunding.setUpdates(Integer.parseInt(page.getHtml().css("li.pro-gengxin > span", "text").toString()));
        crowdfunding.setComments(Integer.parseInt(page.getHtml().css("span#common_comment_count", "text").toString()));
        crowdfunding.setLikes(Integer.parseInt(page.getHtml().css("div.nav-wrap-inner > ul.nav-right > li.atten > span", "text").toString()));
        return crowdfunding;
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    public static void main(String[] args) {
        Spider.create(new CrowdfundingPageProcessor())
                .addUrl("https://zhongchou.modian.com/publishing/top_money/success")
                .run();
    }
}
