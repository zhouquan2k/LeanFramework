package io.leanddd.component.data.impl;

import io.leanddd.component.data.BaseMapper;
import io.leanddd.component.data.DictionaryItem;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

// from dictionary table in database
public class DictionaryProvider<T extends DictionaryItem> {

    static List<DictionaryProvider<?>> allProviders = new Vector<DictionaryProvider<?>>();

    final BaseMapper<T> accessor;

    // 'type -> list of dictionary items' provided from this provider
    Map<String, List<DictionaryItem>> dictionaries;

    public DictionaryProvider(BaseMapper<T> accessor) {
        this.accessor = accessor;
        allProviders.add(this);
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    private Map<String, List<DictionaryItem>> fetch() {
        List<T> data = accessor.queryAll();
        return data.stream().collect(Collectors.groupingBy(dict -> dict.getType()));
    }

    public Map<String, List<DictionaryItem>> getDictionaries() {
        if (dictionaries.size() == 0) this.refresh();
        return dictionaries;
    }

    public void refresh() {
        this.dictionaries = fetch();
    }
}

