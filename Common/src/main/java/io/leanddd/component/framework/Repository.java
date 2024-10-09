package io.leanddd.component.framework;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {

    Optional<T> get(String key);

    T save(T t); // create or update, default strategy as spring jdbc data.
    
    void remove(String key);

    void remove(T t);

    void createBatch(List<T> list);

    // from factory
    T create(Object obj);
}

