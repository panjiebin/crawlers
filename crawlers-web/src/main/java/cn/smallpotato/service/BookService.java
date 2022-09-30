package cn.smallpotato.service;

import cn.smallpotato.entity.Book;
import cn.smallpotato.entity.Crowdfunding;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author panjb
 */
public interface BookService extends IService<Book> {

    /**
     * 根据url查询数据
     * @param url url
     * @return 一条数据
     */
    Book queryByUrl(String url);
}
