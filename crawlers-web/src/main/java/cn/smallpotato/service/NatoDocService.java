package cn.smallpotato.service;

import cn.smallpotato.entity.Book;
import cn.smallpotato.entity.NatoDoc;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author panjb
 */
public interface NatoDocService extends IService<NatoDoc> {

    /**
     * 根据url查询数据
     * @param url url
     * @return 一条数据
     */
    NatoDoc queryByUrl(String url);
}
