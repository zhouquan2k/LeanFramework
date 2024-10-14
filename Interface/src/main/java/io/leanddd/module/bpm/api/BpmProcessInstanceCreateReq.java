package io.leanddd.module.bpm.api;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
@Builder
public class BpmProcessInstanceCreateReq {

    @NotEmpty
    private String processDefinitionKey;

    private String instanceName;

    private Map<String, Object> variables;

    @NotEmpty
    private String businessKey;

}
