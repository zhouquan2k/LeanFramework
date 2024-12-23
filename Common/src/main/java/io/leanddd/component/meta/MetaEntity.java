package io.leanddd.component.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MetaEntity {
    String name() default "";
    String tableName() default "";
    String label() default "";
    boolean isBase() default false;
    boolean defaultUpdatable() default false;
}

