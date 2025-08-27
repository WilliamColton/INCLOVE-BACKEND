package forfun.williamcolton.c.inclove.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IBaseService<T> extends IService<T> {

    boolean saveOrUpdate(T entity, Wrapper<T> updateWrapper);

}
