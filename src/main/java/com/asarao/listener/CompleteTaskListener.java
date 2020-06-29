package com.asarao.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;

/*
 * @ClassName: CompleteTaskListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/29 15:00
 * @Version: 1.0
 **/
@Component("completeListener")
@Slf4j
public class CompleteTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        TaskService taskService = delegateTask.getProcessEngineServices().getTaskService();

        UserTask userTask = delegateTask.getBpmnModelElementInstance();
        ExtensionElements extensionElements = userTask.getExtensionElements();
        CamundaProperties camundaProperties = extensionElements.getElementsQuery()
                .filterByType(CamundaProperties.class).singleResult();
        Collection<CamundaProperty> properties = camundaProperties.getCamundaProperties();
        CamundaProperty camundaProperty = properties.iterator().next();
        String notifyUsers = camundaProperty.getCamundaValue();
        String[] users = notifyUsers.split(",");

        DelegateExecution execution = delegateTask.getExecution();

        DelegateExecution superExecution = execution.getSuperExecution();
        if(superExecution != null){
            System.out.println(superExecution.getProcessBusinessKey());
        }

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
            taskService.complete(task.getId());
            log.info("抄送任务完成");
        }

    }
}
