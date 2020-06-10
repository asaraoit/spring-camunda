package com.asarao;

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

import java.util.List;

/*
 * @ClassName: SuspendAndActiveTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/10 14:49
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class SuspendAndActiveTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/suspend-active.bpmn")
                .name("挂起(暂停)/激活测试")
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
        String instanceKey = "suspend-active";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey);
        System.out.println("流程实例ID："+processInstance.getId());
        System.out.println("流程实例业务Key："+processInstance.getBusinessKey());
        System.out.println("流程实例CaseInstanceId："+processInstance.getCaseInstanceId());
        System.out.println("流程定义ID："+processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID："+processInstance.getProcessInstanceId());
        System.out.println("流程实例TenantID："+processInstance.getTenantId());
        System.out.println("流程实例is Suspended："+processInstance.isSuspended());
    }


    @Test
    public void suspend(){
        String processInstanceId = "62dc671f-aae7-11ea-865e-000ec6dd34b8";
        runtimeService.suspendProcessInstanceById(processInstanceId);
        System.out.println("暂停/挂起成功");
    }

    @Autowired
    TaskService taskService;


    @Test
    public void complete(){
        String taskId = "62deb112-aae7-11ea-865e-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("完成任务");
    }

    @Test
    public void active(){
        String processInstanceId = "62dc671f-aae7-11ea-865e-000ec6dd34b8";
        runtimeService.activateProcessInstanceById(processInstanceId);
        System.out.println("激活成功");
    }
}
