package io.leanddd.component.meta;

public interface EnumDescription {

    String getDesc();

    default EnumTag getTag() {
        return null;
    }
}
