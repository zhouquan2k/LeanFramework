package io.leanddd.component.data.helper;

import io.leanddd.component.common.BizException;
import io.leanddd.component.data.BaseEntity;
import io.leanddd.component.event.EntityCreatedEvent;
import io.leanddd.component.event.EntityDeletedEvent;
import io.leanddd.component.event.EntityUpdatedEvent;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.Repository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CrudServiceImpl<T extends BaseEntity> { // implements CrudService

    protected final Repository<T> repository;

    // @Override
    public T getById(String id) {
        return repository.get(id).orElseThrow();
    }

    // @Override
    public T create(T one) {
        var entity = (T) repository.create(one);
        var newEntity = (T) repository.save(entity);
        Context.publishEvent(new EntityCreatedEvent(newEntity));
        return newEntity;
    }

    // @Override
    public T update(String id, T one) {
        var entity = repository.get(id).orElseThrow();
        entity.update(one);
        entity = (T) repository.save(entity);
        Context.publishEvent(new EntityUpdatedEvent(entity));
        return entity;
    }

    // @Override
    public void delete(String id) throws BizException {
        var entity = repository.get(id).orElseThrow();
        repository.remove(id);
        Context.publishEvent(new EntityDeletedEvent(entity));
    }
}

