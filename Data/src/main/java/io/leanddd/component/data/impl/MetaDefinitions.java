package io.leanddd.component.data.impl;

import io.leanddd.component.common.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Data
@NoArgsConstructor
@AllArgsConstructor
class AnnotationDesc {
    String className;
    AnnotationProperty[] properties;
    Predicate<Map<String, Object>> enable;

    public AnnotationDesc(String className, AnnotationProperty[] properties) {
        this.className = className;
        this.properties = properties;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class AnnotationProperty {
    String name; // dest
    Object value; // src
    String metaProp; // src

    public AnnotationProperty(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class AnnotationMap {
    String myAnnotaion;
    AnnotationDesc[] destAnnotations;
}

@Slf4j
public class MetaDefinitions {

    static final String rootPakcge = "io.leanddd";
    static final String swaggerApi = "io.swagger.v3.oas.annotations.tags.Tag";
    static final String swaggerOperation = "io.swagger.v3.oas.annotations.Operation";
    static final String swaggerModel = "io.swagger.v3.oas.annotations.media.Schema";
    static final String DBTypeEnum = "Enum:com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant";
    static final String TableAnnotationClass = "com.gitee.sunchenbin.mybatis.actable.annotation.Table";
    static final String ColumnAnnotationClass = "com.gitee.sunchenbin.mybatis.actable.annotation.Column";

    // TODO refactor to config file, like xml/yml file
    // annotation on class/methods
    static Map<String, List<AnnotationMap>> annotationMaps = Map
            .of(rootPakcge + ".component.meta.Service", List.of(
                    new AnnotationMap("_class", new AnnotationDesc[]{
                            new AnnotationDesc("org.springframework.stereotype.Service", new AnnotationProperty[]{}),
                            new AnnotationDesc(swaggerApi,
                                    new AnnotationProperty[]{
                                            new AnnotationProperty("name", null, "name")}),
                            new AnnotationDesc("org.springframework.web.bind.annotation.RestController",
                                    new AnnotationProperty[]{}),}),

                    new AnnotationMap("_class_method",
                            new AnnotationDesc[]{new AnnotationDesc(rootPakcge + "component.meta.Command",
                                    new AnnotationProperty[]{}, (data) -> {
                                return Objects.equals("" + data.get("type"), "Command");
                            }), new AnnotationDesc(rootPakcge + "component.meta.Query",
                                    new AnnotationProperty[]{}, (data) -> {
                                return Objects.equals("" + data.get("type"), "Query");
                            })}),

                    new AnnotationMap(rootPakcge + ".component.meta.Command",
                            new AnnotationDesc[]{
                                    new AnnotationDesc(swaggerOperation,
                                            new AnnotationProperty[]{
                                                    new AnnotationProperty("summary", null, "value")}),

                                    new AnnotationDesc("org.springframework.transaction.annotation.Transactional",
                                            new AnnotationProperty[]{}, data -> {
                                        return !Objects.equals("None", "" + data.get("transaction"));
                                    }),
                            }),
                    new AnnotationMap(rootPakcge + ".component.meta.Query",
                            new AnnotationDesc[]{
                                    new AnnotationDesc(swaggerOperation,
                                            new AnnotationProperty[]{
                                                    new AnnotationProperty("summary", null, "value")}),
                                    new AnnotationDesc("org.springframework.transaction.annotation.Transactional",
                                            new AnnotationProperty[]{}, data -> {
                                        return !Objects.equals("None", "" + data.get("transaction"));
                                    }),})

            ));
    static Map<String, Map<String, Object>> Categories = Map.of(
            "DisplayName",
            Map.of("type", "String", "length", 50, "searchable", true, "listable", true, "editable", true, "label",
                    "显示名称"), //
            "Phone",
            Map.of("type", "String", "length", 20, "searchable", true, "listable", true, "editable", true, "label",
                    "电话"), //
            "PersonName",
            Map.of("type", "String", "length", 20, "searchable", true, "listable", true, "editable", true, "label",
                    "姓名"), //
            "Password", Map.of("type", "String", "length", 100, "hidden", true, "editable", false, "label", "密码", "listable", false)
    );
    static Map<String, Map<String, Object>> AllTypeDefaults = Util.<Map<String, Object>>initMap(new Object[][]{ //
            {"ID", Map.of("hidden", true, "isNull", false, "editable", false)}, //
            {"IDStr", Map.of("hidden", true, "isNull", false, "editable", false)}, //
            {"Date", Map.of("hidden", false, "colWidth", 100)}, //
            {"RefID", Map.of("hidden", true, "editable", false)}, //
            {"RefIDStr", Map.of("hidden", true, "editable", false)}, //
            {"JSON", Map.of("listable", false, "searchable", false)}, //
            {"Enum", Map.of("listable", true, "searchable", true)}, //
            {"Dictionary", Map.of("listable", true, "searchable", true)}, //
            {"Default", Map.of("hidden", true, "editable", false)}, //
            {"ToMany", Map.of("hidden", true, "editable", false)}, //
            {"ToOne", Map.of("hidden", true, "editable", false)}});
    static AnnotationDesc[] NoPersistent = new AnnotationDesc[]{
            new AnnotationDesc("org.springframework.data.annotation.Transient",
                    new AnnotationProperty[]{})
    };
    // fields/properties annotations
    static Map<String, Object> metaMap = Util.initMap(
            new Object[][]{
                    {"_class",
                            new AnnotationDesc[]{
                                    new AnnotationDesc(swaggerModel,
                                            new AnnotationProperty[]{
                                                    new AnnotationProperty("description", null, "label"),}),
                                    new AnnotationDesc(TableAnnotationClass,
                                            new AnnotationProperty[]{
                                                    new AnnotationProperty("name", null, "tableName")},
                                            data -> Util.isNotEmpty((String) data.get("tableName"))),
                                    new AnnotationDesc("org.springframework.data.relational.core.mapping.Table",
                                            new AnnotationProperty[]{
                                                    new AnnotationProperty("value", null, "tableName")},
                                            data -> Util.isNotEmpty((String) data.get("tableName")))}},
                    {"_common",
                            new AnnotationDesc[]{new AnnotationDesc(swaggerModel,
                                    new AnnotationProperty[]{new AnnotationProperty("description", null, "label"),}),
                                    new AnnotationDesc("com.gitee.sunchenbin.mybatis.actable.annotation.DefaultValue",
                                            new AnnotationProperty[]{
                                                    new AnnotationProperty("value", null, "defaultValue")},
                                            data -> Util.isNotEmpty((String) data.get("defaultValue"))),
                                    new AnnotationDesc("com.gitee.sunchenbin.mybatis.actable.annotation.Unique",
                                            new AnnotationProperty[]{
                                                    new AnnotationProperty("columns", null, "unique")},
                                            data -> ((String[]) data.get("unique")).length > 0)}},
                    {"ID", new AnnotationDesc[]{
                            new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "BIGINT", DBTypeEnum),
                                            new AnnotationProperty("isKey", true),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", false),
                                            new AnnotationProperty("isAutoIncrement", true)}),
                            new AnnotationDesc("org.springframework.data.annotation.Id",
                                    new AnnotationProperty[]{})}},
                    {"IDStr",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "VARCHAR", DBTypeEnum),
                                            new AnnotationProperty("length", 50), new AnnotationProperty("isKey", true),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", false)}),
                                    new AnnotationDesc("org.springframework.data.annotation.Id",
                                            new AnnotationProperty[]{})
                            }},
                    {"RefID",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "BIGINT", DBTypeEnum),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),})}},
                    {"RefIDStr",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "VARCHAR", DBTypeEnum),
                                            new AnnotationProperty("length", 50, "length"),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull")})}},
                    {"Enum",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "VARCHAR", DBTypeEnum),
                                            new AnnotationProperty("length", 50),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),})}},
                    {"Dictionary",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "VARCHAR", DBTypeEnum),
                                            new AnnotationProperty("length", 50),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),})}},

                    {"String",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "VARCHAR", DBTypeEnum),
                                            new AnnotationProperty("length", 50, "length"),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),})}},
                    {"StringList",
                            new AnnotationDesc[]{new AnnotationDesc(
                                    "com.gitee.sunchenbin.mybatis.actable.annotation.Column",
                                    new AnnotationProperty[]{new AnnotationProperty("type", "JSON", DBTypeEnum),
                                            new AnnotationProperty("length", 256, "length"),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull")})}
                    },
                    {"Date",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "DATE", DBTypeEnum),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),}),
                                    new AnnotationDesc("com.fasterxml.jackson.annotation.JsonFormat",
                                            new AnnotationProperty[]{new AnnotationProperty("pattern", "yyyy-MM-dd",
                                                    null),
                                                    new AnnotationProperty("timezone", System.getenv().get("TZ"), null)
                                            }),
                            }},
                    {"Month",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "VARCHAR", DBTypeEnum),
                                            new AnnotationProperty("length", 10, "length"),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),})}},
                    {"Timestamp",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "DATETIME", DBTypeEnum),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),}),
                                    new AnnotationDesc("com.fasterxml.jackson.annotation.JsonFormat",
                                            new AnnotationProperty[]{new AnnotationProperty("pattern", Util.datetimeFormatterString,
                                                    null),
                                                    new AnnotationProperty("timezone", System.getenv().get("TZ"), null)
                                            })
                            }},
                    {"Decimal",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "DECIMAL", DBTypeEnum),
                                            new AnnotationProperty("length", 20, "length"),
                                            new AnnotationProperty("decimalLength", 3, "decimalLength"),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),})}},
                    {"Integer",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "INT", DBTypeEnum),
                                            new AnnotationProperty("length", 10, "length"),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),})}},
                    {"Text",
                            new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                                    new AnnotationProperty[]{new AnnotationProperty("type", "TEXT", DBTypeEnum),
                                            new AnnotationProperty("comment", null, "label"),
                                            new AnnotationProperty("isNull", null, "isNull"),})}},
                    {"JSON", new AnnotationDesc[]{new AnnotationDesc(ColumnAnnotationClass,
                            new AnnotationProperty[]{new AnnotationProperty("type", "JSON", DBTypeEnum),
                                    new AnnotationProperty("comment", null, "label"),
                                    new AnnotationProperty("isNull", null, "isNull"),}),
                    }},
                    {"ToMany", new AnnotationDesc[]{
                            new AnnotationDesc("org.springframework.data.relational.core.mapping.MappedCollection",
                                    new AnnotationProperty[]{new AnnotationProperty("idColumn", null, "idCol"/* "refId" */),
                                            new AnnotationProperty("keyColumn", "index")}
                                    , data -> (Boolean) data.get("persistable") != Boolean.FALSE)

                    }},
                    {"ToOne",
                            new AnnotationDesc[]{
                                    new AnnotationDesc("org.springframework.data.relational.core.mapping.MappedCollection",
                                            new AnnotationProperty[]{new AnnotationProperty("idColumn", null, "idCol")}
                                            , data -> (Boolean) data.get("persistable") != Boolean.FALSE)
                            }}
            });
}
