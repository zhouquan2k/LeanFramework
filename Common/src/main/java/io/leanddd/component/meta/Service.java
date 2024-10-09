package io.leanddd.component.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

enum DefaultPermissions {
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    /**
     * label
     */
    String value() default "";

    public enum Type {
        Command, Query, Mixed, Base
    }

    /**
     * Type of the service
     */
    Type type() default Type.Mixed;

    /**
     * Used as default route path
     */
    String name() default "";

    /**
     * Default as name
     */
    String permissionDomain() default "";

    /**
     * Permissions class
     */
    Class<?> permissions() default DefaultPermissions.class;

    /**
     * Order show in the metadata, -1 means not shown anywhere, >100 means shown in the permission list, but not in doc
     */
    int order() default -1;
}

