package cn.smallpotato.service.impl;

import cn.smallpotato.entity.NatoDoc;
import cn.smallpotato.mapper.NatoDocMapper;
import cn.smallpotato.service.NatoDocService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author panjb
 */
@Service
public class NatoDocServiceImpl extends ServiceImpl<NatoDocMapper, NatoDoc> implements NatoDocService {

    @Override
    public NatoDoc queryByUrl(String url) {
        QueryWrapper<NatoDoc> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", url);
        List<NatoDoc> list = this.baseMapper.selectList(queryWrapper);
        return list.isEmpty() ? null : list.get(0);
    }
}
