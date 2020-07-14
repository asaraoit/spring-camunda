package com.asarao;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/*
 * @ClassName: SendTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/7/7 10:31
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class Gatewayest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/gateway-test.bpmn")
                .name("发送")
                .deploy();
        System.out.println("部署成功");
    }

    @Autowired
    RuntimeService runtimeService;


    @Test
    public void startProcess(){
        String instanceKey = "gateway-test";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey);
        System.out.println("流程实例ID："+processInstance.getId());
        System.out.println("流程定义ID："+processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID："+processInstance.getProcessInstanceId());
    }

    @Autowired
    TaskService taskService;

    @Test
    public void complete(){
        String taskId = "204251df-c5ae-11ea-84c5-181deaf1ddd1";
        taskService.complete(taskId);
        System.out.println("任务完成");
    }

    @Test
    public void completeTask(){
        String taskId = "07f06e53-c5ae-11ea-ab86-181deaf1ddd1";
        Map<String,Object> variables = new HashMap<>(0);
        variables.put("x",2);
        taskService.complete(taskId,variables);
        System.out.println("任务完成");
    }
}
