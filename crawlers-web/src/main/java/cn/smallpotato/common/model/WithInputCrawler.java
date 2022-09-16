package cn.smallpotato.common.model;

/**
 * @author panjb
 */
public interface WithInputCrawler<IN, OUT extends Element> extends Crawler {

    Iterable<OUT> crawling(IN task);
}
