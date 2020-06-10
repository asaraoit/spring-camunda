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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * @ClassName: PreemptionTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/10 16:18
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class PreemptionTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/Preemption.bpmn")
                .name("抢占式测试")
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
        String instanceKey = "preemption";
        String[] users = {"A","B","C"};
        Map<String,Object> variables = new HashMap<>();
        variables.put("users", Arrays.asList(users));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey, variables);
        System.out.println("流程实例ID："+processInstance.getId());
        System.out.println("流程实例业务Key："+processInstance.getBusinessKey());
        System.out.println("流程实例CaseInstanceId："+processInstance.getCaseInstanceId());
        System.out.println("流程定义ID："+processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID："+processInstance.getProcessInstanceId());
        System.out.println("流程实例TenantID："+processInstance.getTenantId());
        System.out.println("流程实例is Suspended："+processInstance.isSuspended());
    }

    @Test
    public void searchTask(){
        String candidateUser = "A";
        Task task =
                taskService.createTaskQuery().taskCandidateUser(candidateUser).singleResult();
        System.out.println("任务ID：" + task.getId());
    }

    @Autowired
    TaskService taskService;

    @Test
    public void  preemptive(){
        String taskId = "606d6ba1-aaf3-11ea-a321-000ec6dd34b8";
        String userId = "A";
        taskService.claim(taskId,userId);
        System.out.println("签收任务成功");
    }

    @Test
    public void complete(){
        String taskId = "606d6ba1-aaf3-11ea-a321-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("任务完成");
    }
}
