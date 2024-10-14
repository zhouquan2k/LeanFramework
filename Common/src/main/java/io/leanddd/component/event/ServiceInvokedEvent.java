package io.leanddd.component.event;

import io.leanddd.component.framework.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceInvokedEvent implements Event {

    String serviceName;
    String methodName;
    Object[] parameters;
    Object returnValue;
    String taskId;
}
