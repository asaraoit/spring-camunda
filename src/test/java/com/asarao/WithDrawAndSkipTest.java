package com.asarao;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * @ClassName: WithDrawAndSkipTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/11 14:17
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class WithDrawAndSkipTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy() {
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/withdraw-skip.bpmn")
                .name("撤回-跳转")
                .deploy();
        System.out.println("流程部署ID: " + deployment.getId());
        System.out.println("流程部署NAME: " + deployment.getName());
        System.out.println("流程部署Source: " + deployment.getSource());
        System.out.println("流程部署TenantId: " + deployment.getTenantId());
        System.out.println("流程部署Time: " + deployment.getDeploymentTime());
    }

    @Autowired
    RuntimeService runtimeService;

    @Test
    public void startProcess() {
        String instanceKey = "withdraw-skip";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey);
        System.out.println("流程实例ID：" + processInstance.getId());
        System.out.println("流程实例业务Key：" + processInstance.getBusinessKey());
        System.out.println("流程实例CaseInstanceId：" + processInstance.getCaseInstanceId());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID：" + processInstance.getProcessInstanceId());
        System.out.println("流程实例TenantID：" + processInstance.getTenantId());
        System.out.println("流程实例is Suspended：" + processInstance.isSuspended());
    }

    @Autowired
    TaskService taskService;

    @Test
    public void complete() {
        String taskId = "751605fd-ab8c-11ea-a505-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("任务完成");
    }

    @Test
    public void cancel() {
        String taskId = "54824728-abac-11ea-941c-000ec6dd34b8";
        //获取当前任务对象
        Task currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (currentTask == null) {
            throw new RuntimeException("当前任务不存在或已被办理完成，回退失败！");
        }
        //获取流程定义id
        String processDefinitionId = currentTask.getProcessDefinitionId();
        //获取bpmn模板
        BpmnModelInstance bpmnModel = repositoryService.getBpmnModelInstance(processDefinitionId);

//        //获取目标节点定义
//        ModelElementInstance modelElementById = bpmnModel.getModelElementById("");
//        FlowNode targetNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement("受理部门收件");
//
////删除当前运行任务
//        String executionEntityId = managementService.executeCommand(new DeleteTaskCmd(currentTask.getId()));
////流程执行到目标节点
//        managementService.executeCommand(new SetFLowNodeAndGoCmd(targetNode, executionEntityId));

    }

}
