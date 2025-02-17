package io.leanddd.component.data;

import io.leanddd.component.common.Pair;
import io.leanddd.component.common.Util;
import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.MetadataProvider;
import io.leanddd.component.meta.Meta.Type;
import io.leanddd.component.meta.Metadata.EntityDef;
import io.leanddd.component.meta.Metadata.FieldDef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class EntityHelper<T> {
    private static Map<String, EntityHelper<?>> allEntityHelpers = new HashMap<String, EntityHelper<?>>();
    private final Class<T> entityClass;
    private MetadataProvider metadataProvider;
    private EntityDef entityDef;
    private Map<String, PropertyDescriptor> propertyMap;
    private Map<String, Field> fieldMap = new HashMap<String, Field>();
    private List<FieldDef> searchableFields;
    private FieldDef idField = null;

    @SuppressWarnings("unchecked")
    public static <T> EntityHelper<T> getInstance(Class<T> entityClass) {
        EntityHelper<T> helper = (EntityHelper<T>) allEntityHelpers.get(entityClass.getSimpleName());
        if (helper == null) {
            helper = new EntityHelper<T>(entityClass);
            helper.init();
            allEntityHelpers.put(entityClass.getSimpleName(), helper);
        }
        return helper;
    }

    public static EntityHelper<?> getInstance(String entityName) {
        return allEntityHelpers.get(entityName);
    }

    public EntityHelper<T> init() {
        if (this.metadataProvider == null) {
            this.metadataProvider = Context.getBean(MetadataProvider.class);
        }
        this.entityDef = metadataProvider.getEntityDef(entityClass.getName());

        // remove duplicated by name
        this.searchableFields = new ArrayList<>(getSearchableFields(this.entityDef, "a", 0).stream().collect(
                Collectors.toMap(FieldDef::getName, Function.identity(), (field1, field2) -> field1.get_depth() < field2.get_depth() ? field1 : field2) // merge
        ).values());

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(entityClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            this.propertyMap = Util.toMap(Arrays.stream(propertyDescriptors), property -> property.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Stream.of(this.entityClass.getDeclaredFields()).forEach(field -> {
            fieldMap.put(field.getName(), field);
        });

        this.entityDef.getFields().forEach(field -> {
            try {
                if (field.getType() == Type.ID || field.getType() == Type.IDStr)
                    this.idField = field;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return this;
    }

    private List<FieldDef> getSearchableFields(EntityDef entityDef, String alias, int depth) {
        return Util.toList(entityDef.getFields().stream().filter(field -> field.isSearchable() || Objects.equals(entityDef.getIdField(), field.getName())).flatMap(field -> {
            if (field.getType() == Type.ToMany || field.getType() == Type.ToOne || (!field.isPersistable() && field.getType() == Type.Default)) {
                EntityDef subEntityDef = metadataProvider.getEntityDef(field.getFullTypeName());
                return subEntityDef != null ? getSearchableFields(subEntityDef, field.getName(), depth + 1).stream() : Stream.of();
            } else {
                var f = field.clone();
                f.set_depth(depth);
                f.setAlias(alias);
                return Stream.of(f);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object obj) {
        if (obj instanceof Map)
            return ((Map<String, Object>) obj);
        else if (this.entityClass.isInstance(obj)) {
            return this.propertyMap.keySet().stream().collect(Collectors.toMap(key -> key, key -> getPropertyValue(obj, key)));
        } else {
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                return Util.toMap(Arrays.stream(propertyDescriptors), property -> property.getName(), property -> {
                    try {
                        return property.getReadMethod().invoke(obj);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object getPropertyValue(Object obj, String propertyName) {
        Object value = null;
        try {
            if (obj instanceof Map) {
                value = ((Map<String, Object>) obj).get(propertyName);
            } else if (this.entityClass.isInstance(obj)) {
                Util.check(propertyMap.containsKey(propertyName), "invalid propertyName: " + propertyName);
                value = propertyMap.get(propertyName).getReadMethod().invoke(obj);
            } else
                value = Util.getBeanProperty(obj, propertyName);
        } catch (Exception e) {
            throw new RuntimeException("property: " + propertyName, e);
        }
        return value;
    }

    public String queryByExample(Object example, String selectPart, Map<String, Object> fixedParams, List<String> customConditions) {
        if (selectPart == null)
            selectPart = String.format("select * from %s a ${where} order by a.updated_time desc",
                    entityDef.getTableName());

        Stream<String> conditions = getConditions(example, fixedParams);
        if (customConditions != null) {
            conditions = Stream.concat(conditions, customConditions.stream());
        }
        String wherePart = "";

        wherePart = conditions.collect(Collectors.joining(" and "));
        if (wherePart.length() > 0) {
            wherePart = ((selectPart.indexOf("where ") > 0) ? " and " : " where ") + wherePart;
        }

        String sql = Util.format(selectPart, Map.of("where", wherePart));
        log.debug("queryByExample: " + sql);
        return sql;
    }

    @SuppressWarnings("unchecked")
    private Stream<String> getConditions(Object example, Map<String, Object> fixedParams) {
        String paramName = "example";
        var valueMap = toMap(example);
        List<FieldDef> colFieldList = Util
                .toList(searchableFields.stream().filter(field -> getPropertyValue(valueMap, field.getName()) != null)
                        .filter(field -> fixedParams == null || !fixedParams.containsKey(field.getName())));

        return colFieldList.stream().map(field -> {
            if (Set.of(Type.String, Type.Text).contains(field.getType()))
                return String.format("%s.%s like concat('%%', #{%s.%s}, '%%')", field.getAlias(), field.getDbColName(),
                        paramName, field.getName());
            if (field.getType() == Type.Enum) {
                Object value = valueMap.get(field.getName());
                if (value instanceof List) {
                    var list = (List<Object>) value;
                    return list.size() > 0 ? String.format("%s.%s in (%s)", field.getAlias(), field.getDbColName(),
                            list.stream().map(i -> String.format("'%s'", i)).collect(Collectors.joining(", "))) : null;
                }
            } else if (field.getType() == Type.Date || field.getType() == Type.Timestamp) {
                Object value = valueMap.get(field.getName());
                if (value instanceof List) {
                    var list = (List<Object>) value;
                    return String.format("%s.%s between '%s' and '%s'", field.getAlias(), field.getDbColName(),
                            list.get(0), list.get(1));
                }
            }
            return String.format("%s.%s = #{%s.%s}", field.getAlias(), field.getDbColName(), paramName,
                    field.getName());
        }).filter(condition -> condition != null);
    }

    public String insert(Object row, String paramName) {
        List<Pair> params = Util.mapToList(
                this.entityDef.getFields().stream()
                        .filter(field -> !field.isHidden() && getPropertyValue(row, field.getName()) != null),
                field -> new Pair(field.getDbColName(), field.getName()));

        String cols = params.stream().map(param -> param.getKey()).collect(Collectors.joining(", "));
        String values = params.stream().map(param -> String.format("#{%s.%s}", paramName, param.getValue()))
                .collect(Collectors.joining(", "));
        String sql = String.format("insert into %s (%s) values (%s) ", entityDef.getTableName(), cols, values);
        return sql;
    }

    private boolean canUpdate(FieldDef field) {
        return !Set.of(Type.ToMany, Type.ToOne, Type.ID, Type.IDStr).contains(field.getType())
                && field.isPersistable() && !field.isImmutable();
    }

    // called by Repository.update, to generate update sql
    public void executeUpdate(JdbcTemplate jdbcTemplate, Object po) {
        List<Pair> paramPairs = this.entityDef.getFields().stream().filter(field -> canUpdate(field)).map(
                field -> new Pair(field.getDbColName(), getPropertyValue(po, field.getName()))).collect(Collectors.toList());

        String sets = paramPairs.stream().map(param -> String.format("%s = ?", param.getKey()))
                .collect(Collectors.joining(", "));

        String sql = String.format("update %s set %s where %s = ?", entityDef.getTableName(), sets,
                this.idField.getDbColName());
        log.debug(sql);
        List<Object> params = new ArrayList<Object>(Util.mapToList(paramPairs.stream(), pair -> {
            var value = pair.getValue();
            if (value instanceof Enum) {
                return ((Enum<?>) value).name();
            }
            return value;
        }));
        params.add(getPropertyValue(po, this.entityDef.getIdField()));
        jdbcTemplate.update(sql, params.toArray());
    }

    // called by domain.update, to copy properties to domain
    public void update(Object target, Object src) {
        Util.check(target.getClass() == entityClass);
        // only support simple type, you must assign objects manually
        this.entityDef.getFields().stream().filter(field -> field.isEditable()).forEach(field -> {
            Object value;
            try {
                value = this.getPropertyValue(src, field.getName());
                // null means keep un updated.
                if (value != null) {
                    var writeMethod = this.propertyMap.get(field.getName()).getWriteMethod();
                    if (writeMethod != null)
                        writeMethod.invoke(target, value);
                    else {
                        var theField = this.fieldMap.get(field.getName());
                        theField.setAccessible(true);
                        theField.set(target, value);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

