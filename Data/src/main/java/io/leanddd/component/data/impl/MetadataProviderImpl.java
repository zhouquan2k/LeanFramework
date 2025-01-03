package io.leanddd.component.data.impl;

import io.leanddd.component.common.BizException;
import io.leanddd.component.common.Util;
import io.leanddd.component.framework.MetadataProvider;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.Metadata;
import io.leanddd.component.meta.Metadata.DictionaryItemDef;
import io.leanddd.component.meta.Metadata.EntityDef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class MetadataProviderImpl implements MetadataProvider {

    private final MessageSource messageSource;

    @Override
    public Map<String, DictionaryItemDef> getDictionary(String dictionaryName, Locale locale) {
        Metadata metadata = this.getMetadata(locale, null);
        return metadata.getDictionaries().get(dictionaryName).stream()
                .collect(Collectors.toMap(item -> (String) item.getValue(), item -> item));
    }

    @Override
    public Metadata getMetadata(Locale locale, List<Class<?>> classes) {
        log.info("getMetadata locale: " + locale);
        Metadata metadata = EntityMetaRegistrar.getMetadata();

        metadata.getEntities().forEach(entity -> {
            entity.getFields().forEach(field -> {
                var defaultLabel = Util.isEmpty(field.getLabel()) ? field.getName() : field.getLabel();
                var label = this.messageSource.getMessage(entity.getName() + "." + field.getName(), null, defaultLabel, locale);
                field.setLabel(label);
            });
        });
        metadata.getServices().forEach(service -> {
            var defaultValue = Util.isEmpty(service.getLabel()) ? service.getName() : service.getLabel();
            service.setLabel(this.messageSource.getMessage("Service." + service.getName(), null, defaultValue, locale));
            service.getPermissions().forEach(permissionDef -> {
                var defaultLabel = Util.isEmpty(permissionDef.getLabel()) ? permissionDef.getName() : permissionDef.getLabel();
                var label = this.messageSource.getMessage(String.format("Service.%s.permissions.%s", service.getName(), permissionDef.getName()), null, defaultLabel, locale);
                permissionDef.setLabel(label);
            });
        });

        final Map<String, List<DictionaryItemDef>> dictionaries = new HashMap<>(metadata.getDictionaries());
        dictionaries.forEach((key, dictionary) -> {
            dictionary.forEach(item -> {
                var label = item.getLabel();
                if (Util.isEmpty(label)) {
                    label = this.messageSource.getMessage(key + "." + item.getValue(), null, "" + item.getValue(), locale);
                    item.setLabel(label);
                }
            });
        });
        List<DictionaryProvider<?>> allDictionaryProviders = DictionaryProvider.allProviders;
        allDictionaryProviders.forEach(provider -> {
            provider.getDictionaries().entrySet().stream().forEach(entry -> {
                dictionaries.put(entry.getKey(), Util.mapToList(entry.getValue().stream(),
                        item -> {
                            String label = item.getLabel();
                            return new DictionaryItemDef(item.getValue(), Util.isEmpty(label) ? "" + item.getValue() : label, null);
                        }));
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

