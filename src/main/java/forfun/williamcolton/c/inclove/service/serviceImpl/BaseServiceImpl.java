package forfun.williamcolton.c.inclove.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import forfun.williamcolton.c.inclove.service.IBaseService;

public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements IBaseService<T> {

    @Override
    public boolean saveOrUpdate(T entity, Wrapper<T> updateWrapper) {
        if (!update(entity, updateWrapper)) {
            return save(entity);
        }
        return true;
    }

}
