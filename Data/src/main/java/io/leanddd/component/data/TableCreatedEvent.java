package io.leanddd.component.data;

import org.springframework.context.ApplicationEvent;

public class TableCreatedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    public TableCreatedEvent(Object source) {
        super(source);
    }
}