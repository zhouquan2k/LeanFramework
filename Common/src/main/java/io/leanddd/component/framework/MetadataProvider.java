package io.leanddd.component.framework;

import io.leanddd.component.meta.Metadata;
import io.leanddd.component.meta.Metadata.EntityDef;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface MetadataProvider {
    Map<String, Metadata.DictionaryItemDef> getDictionary(String dictionaryName, Locale locale);

    Metadata getMetadata(Locale locale, List<Class<?>> classes);

    EntityDef getEntityDef(String entityFullClassName);

    EntityDef getEntityDefByName(String entityName);
}
