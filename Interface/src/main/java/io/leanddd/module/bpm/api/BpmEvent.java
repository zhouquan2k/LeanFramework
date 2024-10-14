package io.leanddd.module.bpm.api;

import io.leanddd.component.framework.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BpmEvent implements Event {

    private String processInstanceId;
    private String businessKey;

    public enum EventType {
        Completed,
    }

    private EventType eventType;


}
