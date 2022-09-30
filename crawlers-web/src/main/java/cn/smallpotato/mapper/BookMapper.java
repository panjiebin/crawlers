package cn.smallpotato.mapper;

import cn.smallpotato.entity.Book;
import cn.smallpotato.entity.Crowdfunding;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * @author panjb
 */
@Repository
public interface BookMapper extends BaseMapper<Book> {
}
