package com.asarao;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * @ClassName: CompleteTaskTrace
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/11 10:18
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class CompleteTaskTrace {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/completeTask.bpmn")
                .name("任务完成追踪")
                .deploy();
        System.out.println("流程部署ID: "+deployment.getId());
        System.out.println("流程部署NAME: "+deployment.getName());
        System.out.println("流程部署Source: "+deployment.getSource());
        System.out.println("流程部署TenantId: "+deployment.getTenantId());
        System.out.println("流程部署Time: "+deployment.getDeploymentTime());
    }

    @Autowired
    RuntimeService runtimeService;

    @Test
    public void startProcess(){
        String instanceKey = "complete-task";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey);
        System.out.println("流程实例ID："+processInstance.getId());
        System.out.println("流程实例业务Key："+processInstance.getBusinessKey());
        System.out.println("流程实例CaseInstanceId："+processInstance.getCaseInstanceId());
        System.out.println("流程定义ID："+processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID："+processInstance.getProcessInstanceId());
        System.out.println("流程实例TenantID："+processInstance.getTenantId());
        System.out.println("流程实例is Suspended："+processInstance.isSuspended());
    }

    @Autowired
    TaskService taskService;

    @Test
    public void complete(){
        String taskId = "751605fd-ab8c-11ea-a505-000ec6dd34b8";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.complete(taskId);
        System.out.println("任务完成");
    }
    @Test
    public void cc(){
        String taskId = "3b269107-b08a-11ea-ac05-000ec6dd34b8";
        Task task = taskService.newTask();

        task.setAssignee("抄送人");
        task.setName("抄送任务");
        task.setParentTaskId(taskId);
        taskService.saveTask(task);
    }
}
