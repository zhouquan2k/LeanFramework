package io.leanddd.component.event;

import io.leanddd.component.framework.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EntityCreatedEvent implements Event {
    private Object entity;
}
