package io.leanddd.component.meta;

import io.leanddd.component.common.Util;
import io.leanddd.component.meta.Meta.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {

    List<EntityDef> entities = new Vector<EntityDef>();
    Map<String, List<DictionaryItemDef>> dictionaries = new HashMap<String, List<DictionaryItemDef>>();
    List<ServiceDef> services = new Vector<ServiceDef>();

    @Data
    @AllArgsConstructor
    public static class FieldDef implements Cloneable {
        String name;
        String dbColName;
        String label;
        Type type;
        String typeName;
        String fullTypeName;
        String refData;
        boolean nullable;
        int length;
        boolean hidden;
        boolean immutable;
        boolean persistable;
        boolean editable;
        boolean searchable;
        boolean listable;
        String defaultValue;
        String uiType;
        String alias;
        Integer colWidth;
        int _depth;

        @Override
        public FieldDef clone() {
            try {
                return (FieldDef) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public void init() {
            if (type == Type.Dictionary && Util.isEmpty(refData)) {
                refData = name;
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class EntityDef {
        String name;
        String label;
        String typeName;
        String tableName;
        String idField;
        List<FieldDef> fields = new Vector<FieldDef>();
    }

    @Data
    @AllArgsConstructor
    public static class DictionaryItemDef implements Comparable<DictionaryItemDef> {
        Object value;
        String label;
        String tag;

        @Override
        public int compareTo(DictionaryItemDef o) {
            return value.toString().compareTo(o.value.toString());
        }
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class PermissionDef {
        private final String name;
        private String label;
    }

    @Data
    @AllArgsConstructor
    public static class MethodDef {
        String name;
        String label;
        String description;
        // TODO params
        // TODO return type
    }

    @Data
    @AllArgsConstructor
    public static class ServiceDef {
        String name;
        String typeName;
        String label;
        String permissionDomain;
        List<PermissionDef> permissions;
        int order;
        List<MethodDef> operations;
    }
}

