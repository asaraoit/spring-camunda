package com.asarao.common.cmd;

import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/*
 * @ClassName: SequentialCountersignCmd
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/12 9:57
 * @Version: 1.0
 **/
public class SequentialCountersignCmd implements Command<Void> {

    protected String taskId;

    protected String assignee;

    private boolean isBefore;

    public SequentialCountersignCmd(String taskId,String assignee,boolean isBefore){
        this.taskId = taskId;
        this.assignee = assignee;
        this.isBefore = isBefore;
    }

    @Override
    public Void execute(CommandContext commandContext) {

        // 根据任务ID 获取任务实体
        TaskEntity taskEntity = commandContext.getTaskManager().findTaskById(taskId);

        // 获取流程定义实例
        ProcessDefinitionEntity processDefinition = taskEntity.getProcessDefinition();

        // 当前任务的活动节点
        ActivityImpl activity = processDefinition.findActivity(taskEntity.getTaskDefinitionKey());

        // 当前活动节点的行为 (UserTaskActivityBehavior)
        ActivityBehavior activityBehavior = activity.getActivityBehavior();

        // 获取执行当前任务的执行实例实体
        ExecutionEntity execution = taskEntity.getExecution();

        AbstractBpmnActivityBehavior userTaskActivityBehavior = (AbstractBpmnActivityBehavior) activityBehavior;

        return null;
    }
}
