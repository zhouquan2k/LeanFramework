package io.leanddd.component.framework;

public interface BeanMgr {
    <T> T getBean(Class<T> cls);

    <T> T getBean(String name, Class<T> cls);

    void publishEvent(Event event);

    String getProperty(String name);
}
