package com.asarao;

import com.asarao.service.ProcessService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @ClassName: DelegateTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/9 17:24
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class DelegateTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/delegate-test.bpmn")
                .name("委派测试")
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
        String instanceKey = "delegate-test";
        Map<String,Object> variables = new HashMap<>();
        variables.put("user","委派人");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey, variables);
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

    // 查询任务（根据办理人）
    @Test
    public void searchTask(){
        String assignee = "委派人";
//        String assignee = "被委派人";
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        if(null != tasks && !tasks.isEmpty()){
            for (Task task:tasks) {
                System.out.println("待办任务ID: "+task.getId());
                System.out.println("待办任务定义key: "+task.getTaskDefinitionKey());
                System.out.println("流程实例ID: "+task.getProcessInstanceId());
                System.out.println("流程定义ID: "+task.getProcessDefinitionId());
                System.out.println("待办任务name: "+task.getName());
            }
        }
    }

    @Test
    public void searchTaskByOwner(){
        String owner = "委派人";
        List<Task> tasks = taskService.createTaskQuery().taskOwner(owner).list();
        if(null != tasks && !tasks.isEmpty()){
            for (Task task:tasks) {
                System.out.println("待办任务ID: "+task.getId());
                System.out.println("待办任务定义key: "+task.getTaskDefinitionKey());
                System.out.println("流程实例ID: "+task.getProcessInstanceId());
                System.out.println("流程定义ID: "+task.getProcessDefinitionId());
                System.out.println("待办任务name: "+task.getName());
                System.out.println("待办任务所有者: "+task.getOwner());
                System.out.println("待办任务办理人: "+task.getAssignee());
            }
        }
    }

    @Autowired
    ProcessService processService;

    @Test
    public void delegate(){
        String taskId = "8d18efb2-aa3d-11ea-a1aa-000ec6dd34b8";
        String user = "被委派人";
        processService.delegate(taskId,user);
        System.out.println("委派成功");
    }

    @Test
    public void turn(){
        String taskId = "f758a1ed-aa39-11ea-8dec-000ec6dd34b8";
        String assignee = "转办后的办理人";
        processService.turn(taskId,assignee);
        System.out.println("转办成功");
    }

    @Test
    public void resolveTask(){
        String taskId = "e94bace6-aa39-11ea-851b-000ec6dd34b8";
        taskService.resolveTask(taskId);
        System.out.println("被委派人解决任务");
    }

    @Test
    public void completeTask(){
        String taskId = "e94bace6-aa39-11ea-851b-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("委派人完成任务");
    }
}
