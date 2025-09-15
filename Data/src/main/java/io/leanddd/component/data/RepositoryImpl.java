package io.leanddd.component.data;

import io.leanddd.component.common.Util;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class RepositoryImpl<T> implements Repository<T> {

    private final Class<T> entityClass;
    private final CrudRepository<T, String> springRepository;

    // load from persistence
    @Override
    public Optional<T> get(String key) {
        return springRepository.findById(key);
    }

    @Override
    public T save(T t) {
        if (t instanceof BaseEntity) {
            @SuppressWarnings("unused")
            BaseEntity<?> base = (BaseEntity<?>) t;
            // save 支持根据delFlag删除
            if (base.isDel()) {
                springRepository.delete(t);
                return t;
            }
        }
        autoFill(t);
        t = springRepository.save(t);
        return t;
    }

    private void autoFill(T po) {
        if (BaseEntity.class.isAssignableFrom(entityClass)) {
            Date now = new Date();
            BaseEntity<?> be = (BaseEntity<?>) po;
            if (be.getVersion() == null) {
                // is new
                if (Util.isEmpty(be.getCreatedBy()))
                    be.setCreatedBy(Context.getUserId());
                if (be.getCreatedTime() == null)
                    be.setCreatedTime(now);
            } else {
                be.setUpdatedBy(Context.getUserId());
                be.setUpdatedTime(now);
            }
        }
    }

    @Override
    public void remove(String key) {
        springRepository.deleteById(key);
    }

    @Override
    public void remove(T t) {
        springRepository.delete(t);
    }

    @Override
    public void createBatch(List<T> list) {
        Util.notSupport();
    }


    @Override
    public T create(Object obj) {
        try {
            T ret = null;
            if (obj == null)
                ret = entityClass.newInstance();
            else if (entityClass.isInstance(obj)) {
                ret = (T) obj;
            }
            Util.check(ret != null, "can't create instance of type:%s", entityClass.getName());
            if (ret instanceof BaseEntity)
                ((BaseEntity) ret).init();
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> findAll() {
        return (List<T>) springRepository.findAll();
    }
}
