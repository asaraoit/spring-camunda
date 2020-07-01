package com.asarao.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.springframework.stereotype.Component;

/*
 * @ClassName: TaskCompleteListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/7/1 14:37
 * @Version: 1.0
 **/
@Component
@Slf4j
public class TaskCompleteListener implements TaskListener {

    private FixedValue notifyUsers;

    public FixedValue getNotifyUsers() {
        return notifyUsers;
    }

    public void setNotifyUsers(FixedValue notifyUsers) {
        this.notifyUsers = notifyUsers;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        log.info("任务监听开始；监听事件：{}",delegateTask.getEventName());
        String processInstanceId = delegateTask.getProcessInstanceId();
        RuntimeService runtimeService = delegateTask.getProcessEngineServices().getRuntimeService();
        VariableInstance nrOfInstances = runtimeService.createVariableInstanceQuery()
                .variableName("nrOfInstances")
                .processInstanceIdIn(processInstanceId)
                .singleResult();
        log.info("多实列的个数：{}",nrOfInstances.getValue());
        VariableInstance nrOfCompletedInstances = runtimeService.createVariableInstanceQuery()
                .variableName("nrOfCompletedInstances")
                .processInstanceIdIn(processInstanceId)
                .singleResult();
        log.info("完成实列的个数：{}",nrOfCompletedInstances.getValue());
        System.out.println(notifyUsers.getExpressionText());
    }


}
