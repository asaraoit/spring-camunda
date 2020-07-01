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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * @ClassName: ListenerTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/7/1 14:42
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class ListenerTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/listener.bpmn")
                .name("监听测试")
                .deploy();
        System.out.println("部署成功");
    }

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Test
    public void startProcess(){
        String instanceKey = "listener";
        Map<String,Object> variables = new HashMap<>();
        variables.put("assigneeList",new ArrayList<>(Arrays.asList("李四","王五")));
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey,variables);
        System.out.println("启动成功");
    }

    @Test
    public void complete(){
        String taskId = "2e12cafe-bb76-11ea-9943-181deaf1ddd1";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.createComment(taskId, task.getProcessInstanceId(), "同意");
        taskService.complete(taskId);
        System.out.println("完成任务");
    }
}
