package io.leanddd.module.bpm.api;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class BpmTask {

    private String id;

	private String nodeDefId;

    private String name;

    private String description;

    private String processInstanceId;

    private String businessKey;

    private String assignee;

    private Date createTime;

    private Date claimTime;

    private String owner;

    private int priority;

    private String formKey;

    private Map<String, Object> processVariables;

    //custom

    private boolean isPendingTask = true;

}
