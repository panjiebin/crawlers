package cn.smallpotato.service.impl;

import cn.smallpotato.entity.Book;
import cn.smallpotato.entity.Crowdfunding;
import cn.smallpotato.mapper.BookMapper;
import cn.smallpotato.mapper.CrowdfundingMapper;
import cn.smallpotato.service.BookService;
import cn.smallpotato.service.CrowdfundingService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author panjb
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Override
    public Book queryByUrl(String url) {
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", url);
        List<Book> list = this.baseMapper.selectList(queryWrapper);
        return list.isEmpty() ? null : list.get(0);
    }
}
