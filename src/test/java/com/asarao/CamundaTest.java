package com.asarao;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Comment;
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
 * @ClassName: CamundaTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/8 18:34
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class CamundaTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/fk-mock.bpmn")
                .name("FK MOCK")
                .deploy();
        System.out.println("部署成功");
    }

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Test
    public void startProcess(){
        String instanceKey = "fk-mock";
        String businessKey = "1";
        Map<String,Object> variables = new HashMap<>();
        variables.put("assignee","张三");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey,businessKey, variables);
        System.out.println("启动成功");

        Task task = taskService.createTaskQuery().taskAssignee("张三")
                .processInstanceBusinessKey("1")
                .singleResult();

        taskService.createComment(task.getId(),processInstance.getId(),"提交");
        Map<String,Object> vars = new HashMap<>(0);
        vars.put("assigneeList",new ArrayList<>(Arrays.asList("李四","王五")));
        vars.put("copyUsers",new ArrayList<>(Arrays.asList("1","2")));
        vars.put("projectId","1");
        taskService.complete(task.getId(),vars);
        System.out.println("提交成功");
    }

    @Test
    public void complete(){
        String taskId = "9eae5c4f-b9d9-11ea-baee-181deaf1ddd1";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        taskService.createComment(taskId, task.getProcessInstanceId(), "同意");
        taskService.complete(taskId);
        System.out.println("完成任务");
    }
}
