package io.leanddd.component.data.impl;

import io.leanddd.component.common.BizException;
import io.leanddd.component.common.Util;
import io.leanddd.component.framework.MetadataProvider;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.Metadata;
import io.leanddd.component.meta.Metadata.DictionaryItemDef;
import io.leanddd.component.meta.Metadata.EntityDef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MetadataProviderImpl implements MetadataProvider {

    @Override
    public Map<String, DictionaryItemDef> getDictionary(String dictionaryName) {
        Metadata metadata = this.getMetadata(null);
        return metadata.getDictionaries().get(dictionaryName).stream()
                .collect(Collectors.toMap(item -> (String) item.getValue(), item -> item));
    }

    //TODO cache
    @Override
    public Metadata getMetadata(List<Class<?>> classes) {
        Metadata metadata = EntityMetaRegistrar.getMetadata();

        final Map<String, List<DictionaryItemDef>> dictionaries = new HashMap<>(metadata.getDictionaries());
        List<DictionaryProvider<?>> allDictionaryProviders = DictionaryProvider.allProviders;
        allDictionaryProviders.forEach(provider -> {
            provider.getDictionaries().entrySet().stream().forEach(entry -> {
                dictionaries.put(entry.getKey(), Util.mapToList(entry.getValue().stream(),
                        item -> new DictionaryItemDef(item.getValue(), item.getLabel(), null)));
            });
        });

        // all function as dictionary
        dictionaries.put("AllDictionaries", dictionaries.entrySet().stream().map(
                entry -> new DictionaryItemDef(entry.getKey(), entry.getKey(), null)).sorted().collect(Collectors.toList()));
        dictionaries.put("AllEntities", Util.mapToList(metadata.getEntities().stream(),
                entity -> new DictionaryItemDef(entity.getName(), entity.getName(), null)));
        dictionaries.put("AllFunctions", Util.mapToList(metadata.getServices().stream(),
                func -> new DictionaryItemDef(func.getName(), func.getLabel(), null)));

        if (classes == null) {
            return new Metadata(metadata.getEntities(), dictionaries, metadata.getServices());
        }

        var classNames = Util.mapToList(classes.stream(), cls -> cls.getName());
        var entities = Util
                .toList(metadata.getEntities().stream().filter(entity -> classNames.contains(entity.getTypeName())));
        var functions = Util.toList(
                metadata.getServices().stream().filter(function -> classNames.contains(function.getTypeName())));

        var dictionaryTypes = Util.toList(entities.stream().flatMap(entity -> {
            return entity.getFields().stream()
                    .filter(field -> field.getType() == Type.Enum || field.getType() == Type.Dictionary)
                    .map(field -> field.getType() == Type.Enum ? field.getTypeName() : field.getRefData());
        }));
        var filteredDictionaries = Util.toMap(
                dictionaries.entrySet().stream()
                        .filter(entry -> dictionaryTypes.contains(entry.getKey())),
                entry -> entry.getKey(), entry -> entry.getValue());

        return new Metadata(entities, filteredDictionaries, functions);
    }

    @Override
    public EntityDef getEntityDef(String fullClassName) {
        Metadata metadata = EntityMetaRegistrar.getMetadata();
        return metadata.getEntities().stream().filter(entity -> Objects.equals(fullClassName, entity.getTypeName())).findFirst()
                .orElseThrow(() -> new BizException(String.format("%s not found", fullClassName)));
    }

    @Override
    public EntityDef getEntityDefByName(String entityName) {
        Metadata metadata = EntityMetaRegistrar.getMetadata();
        return metadata.getEntities().stream().filter(entity -> Objects.equals(entityName, entity.getName())).findFirst()
                .orElseThrow(() -> new BizException(String.format("%s not found", entityName)));
    }
}

