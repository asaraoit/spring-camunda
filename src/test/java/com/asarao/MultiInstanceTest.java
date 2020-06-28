package com.asarao;

import cn.hutool.core.collection.ListUtil;
import com.asarao.service.impl.AssigneeService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * @ClassName: MultiInstanceTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/28 11:48
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiInstanceTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/sequential-multi-instance.bpmn")
                .name("顺序签多实例")
                .deploy();
        System.out.println("部署成功");
    }

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    AssigneeService assigneeService;

    @Test
    public void startProcess(){
        String instanceKey = "sequential-multi-instance";
//        String[] assigneeList = {"A"};
        Map<String,Object> variables = new HashMap<>(0);
//        variables.put("assigneeList", ListUtil.empty());
        variables.put("assigneeService", assigneeService);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey,variables);
        System.out.println("流程实例ID："+processInstance.getId());
        System.out.println("流程定义ID："+processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID："+processInstance.getProcessInstanceId());
    }

    @Autowired
    TaskService taskService;

    @Test
    public void completeTask(){
        String taskId = "e10fc705-b924-11ea-8b48-000ec6dd34b8";
        String[] assigneeList = {"A","B"};
        Map<String,Object> variables = new HashMap<>(0);
        variables.put("assigneeList", Arrays.asList(assigneeList));
        taskService.complete(taskId,variables);
    }
}
