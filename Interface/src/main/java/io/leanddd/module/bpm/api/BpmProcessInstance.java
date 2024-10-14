package io.leanddd.module.bpm.api;

import lombok.Data;

import java.util.List;

@Data
public class BpmProcessInstance {

    String instanceId;

    String businessKey;

	String name;

    int status = 1; // running

    List<BpmTask> tasks;

    List<BpmActivity> activities;
}
