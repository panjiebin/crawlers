package cn.smallpotato.service.impl;

import cn.smallpotato.entity.Crowdfunding;
import cn.smallpotato.mapper.CrowdfundingMapper;
import cn.smallpotato.service.CrowdfundingService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author panjb
 */
@Service
public class CrowdfundingServiceImpl extends ServiceImpl<CrowdfundingMapper, Crowdfunding> implements CrowdfundingService {

    @Override
    public Crowdfunding queryByUrl(String url) {
        QueryWrapper<Crowdfunding> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", url);
        List<Crowdfunding> list = this.baseMapper.selectList(queryWrapper);
        return list.isEmpty() ? null : list.get(0);
    }
}
