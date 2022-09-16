package cn.smallpotato;

import cn.smallpotato.entity.Crowdfunding;
import cn.smallpotato.mapper.CrowdfundingMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class StudentCrawlerApplicationTest {

    @Autowired
    private CrowdfundingMapper crowdfundingMapper;

    @Test
    void testSelectAll() {
        List<Crowdfunding> crowdfundings = crowdfundingMapper.selectList(null);
        System.out.println("size = " + crowdfundings.size());
    }

    @Test
    void testQueryByCdn() {
        QueryWrapper<Crowdfunding> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", "电子竞技");
        List<Crowdfunding> list = crowdfundingMapper.selectList(queryWrapper);
        System.out.println("list.size() = " + list.size());
    }
}