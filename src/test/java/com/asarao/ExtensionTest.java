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

/*
 * @ClassName: ExtensionTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/29 18:18
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class ExtensionTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/extensions-element.bpmn")
                .name("Extensions Element")
                .deploy();
        System.out.println("部署成功");
    }

    @Autowired
    RuntimeService runtimeService;

    @Test
    public void startProcess(){
        String instanceKey = "extensions-element";
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
        String taskId = "9f8e378f-ba02-11ea-9018-181deaf1ddd1";
//        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.complete(taskId);
        System.out.println("任务完成");
    }
}
