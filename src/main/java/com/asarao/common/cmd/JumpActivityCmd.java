package com.asarao.common.cmd;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;

import java.util.ArrayList;
import java.util.List;

/*
 * @ClassName: JumpActivityCmd
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/15 16:28
 * @Version: 1.0
 **/
public class JumpActivityCmd implements Command<Object> {

    private String activityId;
    private String executionId;
    private String taskId;


    public JumpActivityCmd(String activityId, String executionId,String taskId) {
        this.activityId = activityId;
        this.executionId = executionId;
        this.taskId = taskId;
    }


    @Override
    public Object execute(CommandContext commandContext) {
        ExecutionEntity executionEntity = commandContext.getExecutionManager().findExecutionById(executionId);
        ProcessDefinitionImpl processDefinition = executionEntity.getProcessDefinition();
        ActivityImpl activity = processDefinition.findActivity(activityId);
        List<PvmTransition> incomingTransitions = activity.getIncomingTransitions();
        List<PvmTransition> list = new ArrayList<>(incomingTransitions);
        incomingTransitions.clear();
        executionEntity.executeActivity(activity);
        List<PvmTransition> pvmTransitions = activity.getIncomingTransitions();
        pvmTransitions.clear();
        pvmTransitions.addAll(list);
        executionEntity.removeTask(commandContext.getTaskManager().findTaskById(taskId));
        return executionEntity;
    }

}
