package com.asarao.listener;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.instance.CompletionCondition;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.springframework.stereotype.Component;

import java.util.Collection;

/*
 * @ClassName: NotifyListener
 * @Description: 抄送监听，监听任务完成事件 complete
 * @Author: Asarao
 * @Date: 2020/7/1 15:25
 * @Version: 1.0
 **/
@Component
@Slf4j
public class NotifyListener implements TaskListener {

    /**
     * 抄送人员
     */
    private FixedValue notifyUsers;

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("抄送监听执行");
        boolean isLastTask = isLastTask(delegateTask);
        if(isLastTask){
            send(delegateTask);
        }
    }

    // 抄送操作
    private void send(DelegateTask delegateTask) {
        log.info("执行抄送");
        if(notifyUsers == null){
            return;
        }
        String usersStr = notifyUsers.getExpressionText();
        if(StrUtil.isEmpty(usersStr)){ return;}
        String[] users = usersStr.split(",");
        TaskService taskService = delegateTask.getProcessEngineServices().getTaskService();
        for (String user : users) {
            Task task = taskService.newTask();
            task.setParentTaskId(delegateTask.getId());
            task.setAssignee(users[0]);
            task.setDescription("抄送任务");
            TaskEntity taskEntity = (TaskEntity) task;
            // 设置流程ID，查询的时候会用到
            taskEntity.setProcessInstanceId(delegateTask.getProcessInstanceId());
            taskService.saveTask(taskEntity);
            log.info("抄送任务{} 给 [{}]",delegateTask.getId(),user);
            // 直接完成抄送任务
            taskService.complete(task.getId());
        }
        log.info("抄送任务完成");
    }

    /**
     * 判断当前任务是否是最后的执行任务
     */
    private boolean isLastTask(DelegateTask delegateTask){
        // 流程实列的ID
        String processInstanceId = delegateTask.getProcessInstanceId();
        RuntimeService runtimeService = delegateTask.getProcessEngineServices().getRuntimeService();
        // 多实例任务的实例数
        VariableInstance nrOfInstances = runtimeService.createVariableInstanceQuery()
                .variableName("nrOfInstances")
                .processInstanceIdIn(processInstanceId)
                .singleResult();
        if(nrOfInstances != null){
            // 多实例任务
            int nrOfInstancesValue = (int) nrOfInstances.getValue();
            // 获取完成条件
            UserTask userTask = delegateTask.getBpmnModelElementInstance();
            // 扩展元素
            ExtensionElements extensionElements = userTask.getExtensionElements();
            // 多实例配置
            Collection<MultiInstanceLoopCharacteristics> characteristicsList =
                    userTask.getChildElementsByType(MultiInstanceLoopCharacteristics.class);
            MultiInstanceLoopCharacteristics characteristics = characteristicsList.iterator().next();
            CompletionCondition completionCondition = characteristics.getCompletionCondition();
            if(completionCondition == null){
                // 顺序签 或者 会签
                // 完成实例数
                VariableInstance nrOfCompletedInstances = runtimeService.createVariableInstanceQuery()
                        .variableName("nrOfCompletedInstances")
                        .processInstanceIdIn(processInstanceId)
                        .singleResult();
                int nrOfCompletedInstancesValue = (int) nrOfCompletedInstances.getValue();
                // 判断当前任务是否是最后一个任务 --是则执行抄送
                return nrOfInstancesValue - nrOfCompletedInstancesValue == 1;
            }
            // 则为或签--执行抄送
        }
        return true;
    }


    public FixedValue getNotifyUsers() {
        return notifyUsers;
    }

    public void setNotifyUsers(FixedValue notifyUsers) {
        this.notifyUsers = notifyUsers;
    }
}
