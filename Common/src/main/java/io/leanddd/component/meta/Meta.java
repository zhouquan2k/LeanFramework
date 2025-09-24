package io.leanddd.component.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.leanddd.component.meta.Meta.BooleanEx.Default;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Meta {

    Type value() default Type.Default;

    String column() default "";

    BooleanEx nullable() default Default;

    BooleanEx persistable() default Default; // false: is not a database column

    String[] unique() default {};

    String label() default "";

    int length() default -1;

    int colWidth() default -1;

    Category category() default Category.None; // = a type with a group of default properties.

    boolean immutable() default false; // false: won't in the update fields in sql

    String refData() default ""; // "dictionary.institution" "entity.AssemblyFactory"

    // TODO default value definition
    /////////

    // ui related below
    BooleanEx hidden() default Default;

    BooleanEx listable() default Default;

    BooleanEx editable() default Default; // true: ui updatable

    BooleanEx searchable() default Default;

    public enum BooleanEx {
        True, False, Default
    }

    public enum Type {
        ID, IDStr,
        RefID, RefIDStr,
        Enum, Dictionary,
        String, Text,
        StringList,
        JSON,
        Integer, Decimal,
        Timestamp, Date, Time, Month,
        ToOne,
        ToMany,
        Default
    }

    // treat as alias of Type
    public enum Category {
        Password, DisplayName, Phone, PersonName, None,
    }
}
