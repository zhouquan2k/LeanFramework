package io.leanddd.module.bpm.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping("/api/bpm")
public interface BpmService {
    
    String createProcessInstance(BpmProcessInstanceCreateReq req);

    @GetMapping("tasks/my")
    List<BpmTask> queryMyTasks(); // Query...

    @GetMapping("instances/my/history")
    List<BpmProcessInstance> queryMyHistoryInstances();

	@GetMapping("instances/{processInstanceId}/tasks")
	List<BpmTask> getCurrentTasks(@PathVariable String processInstanceId);

	@GetMapping("instances/by-business-key/{businessKey}")
	BpmProcessInstance getInstanceByBusinessKey(@PathVariable String businessKey);

    @GetMapping("tasks/{taskId}")
    BpmTask getTask(@PathVariable String taskId);

    void claimTask(String taskId, String userId);

    @PostMapping("tasks/{taskId}")
    void completeTask(@PathVariable String taskId, Map<String, Object> variables);

}
