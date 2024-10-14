package io.leanddd.module.bpm.api;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class BpmActivity {

    @Data
    public static class VariableUpdate {
        String name;
        String value;
    }

    String activityId;
    String key;
    String type;
    String name;
    Date startTime;
    Date endTime;
    String assignee;
    String username;

    List<VariableUpdate> variables;

}
