package com.asarao;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/*
 * @ClassName: CountersignTest
 * @Description: 会签测试
 * @Author: Asarao
 * @Date: 2020/6/10 9:49
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class CountersignTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/modify-multi-instance.bpmn")
                .name("会签测试")
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
        String instanceKey = "countersign";
        Map<String,Object> variables = new HashMap<>();
        variables.put("assignee","提交者");
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
    public void startProcess2(){
        String instanceKey = "modify-multi-instance";
        String[] assigneeList = {"办理人A","办理人B","办理人C"};
        Map<String,Object> variables = new HashMap<>();
        variables.put("assigneeList", Arrays.asList(assigneeList));
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

    @Test
    public void completeTask(){
        String taskId = "e730f717-af7d-11ea-9e4b-000ec6dd34b8";
        String[] assigneeList = {"办理人A","办理人B","办理人C","办理人D"};
        Map<String, Object> variables = new HashMap<>(0);
        variables.put("assigneeList", Arrays.asList(assigneeList));
        taskService.complete(taskId,variables);
        System.out.println("提交申请完成");
    }

    @Test
    public void searchVariables(){
        String processInstanceId = "ab979b0f-aac5-11ea-a050-000ec6dd34b8";
        VariableInstance nrOfInstances = runtimeService.createVariableInstanceQuery()
                .variableName("nrOfInstances")
                .processInstanceIdIn(processInstanceId)
                .singleResult();
        if(nrOfInstances != null){
            System.out.println("实例总数："+ nrOfInstances.getValue());
        }else {
            System.out.println("此变量已被清空");
            return;
        }

        VariableInstance nrOfCompletedInstances = runtimeService.createVariableInstanceQuery()
                .variableName("nrOfCompletedInstances")
                .processInstanceIdIn(processInstanceId)
                .singleResult();
        if(nrOfCompletedInstances != null){
            System.out.println("完成实例总数："+ nrOfCompletedInstances.getValue());
        }

        VariableInstance nrOfActiveInstances = runtimeService.createVariableInstanceQuery()
                .variableName("nrOfActiveInstances")
                .processInstanceIdIn(processInstanceId)
                .singleResult();
        if(nrOfActiveInstances != null){
            System.out.println("未完成实例总数："+ nrOfActiveInstances.getValue());
        }

    }

    @Test
    public void complete(){
        String taskId = "dc6161ec-aac5-11ea-ace5-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("完成任务");
    }

    @Test
    public void addInstance(){
        String processInstanceId = "d0a284f3-af8e-11ea-88c4-000ec6dd34b8";

        runtimeService.createProcessInstanceModification(processInstanceId)
                .startBeforeActivity("Activity_1jo1sok")
                .setVariable("assignee","新加签的人")
                .execute();

        System.out.println("会签加签成功");
    }
}
