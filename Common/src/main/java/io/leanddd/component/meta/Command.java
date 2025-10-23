package io.leanddd.component.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String value() default "";

    public enum TransactionType {
        Normal, None
    }

    TransactionType transaction() default TransactionType.Normal;

    boolean authenticated() default false;

    String permission() default "";

    String[] permissions() default {};

    public enum LogType {
        Yes, No, Default
    }

    LogType log() default LogType.Default;

    boolean logParam() default true;
}

