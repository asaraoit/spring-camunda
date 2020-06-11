package com.asarao.common.process;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;

/*
 * @ClassName: JumpCmd
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/11 13:37
 * @Version: 1.0
 **/
public class JumpCmd implements Command<ExecutionEntity> {

    private String processInstanceId;
    private String activityId;
    public static final String REASION_DELETE = "deleted";

    public JumpCmd(String processInstanceId, String activityId) {
        this.processInstanceId = processInstanceId;
        this.activityId = activityId;
    }

    @Override
    public ExecutionEntity execute(CommandContext commandContext) {
        ExecutionEntity executionEntity = commandContext.getExecutionManager().findExecutionById(processInstanceId);
        executionEntity.clearScope(REASION_DELETE,true,true,false);
//        executionEntity.destroyScope(REASION_DELETE);
        ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
        ActivityImpl activity = processDefinition.findActivity(activityId);
        executionEntity.executeActivity(activity);
        return executionEntity;
    }
}
