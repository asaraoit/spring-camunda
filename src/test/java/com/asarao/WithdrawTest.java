package com.asarao;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
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
 * @ClassName: WithdrawTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/9 13:30
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class WithdrawTest {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/withdraw.bpmn")
                .name("测试取回")
                .deploy();
        System.out.println("流程部署ID: "+deployment.getId());
        System.out.println("流程部署NAME: "+deployment.getName());
        System.out.println("流程部署Source: "+deployment.getSource());
        System.out.println("流程部署TenantId: "+deployment.getTenantId());
        System.out.println("流程部署Time: "+deployment.getDeploymentTime());
    }

    @Test
    public void startProcess(){
        String instanceKey = "withdraw";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey);
        System.out.println("流程实例ID："+processInstance.getId());
        System.out.println("流程实例业务Key："+processInstance.getBusinessKey());
        System.out.println("流程实例CaseInstanceId："+processInstance.getCaseInstanceId());
        System.out.println("流程定义ID："+processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID："+processInstance.getProcessInstanceId());
        System.out.println("流程实例TenantID："+processInstance.getTenantId());
        System.out.println("流程实例is Suspended："+processInstance.isSuspended());
    }

    // 查询任务（根据办理人）
    @Test
    public void searchTask(){
        String assignee = "A";
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
    public void completeTask(){
        String taskId = "86bd22df-aa27-11ea-b4ff-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("任务：["+ taskId+"] 完成");
    }

    @Autowired
    HistoryService historyService;

    @Test
    public void getHistoricTaskById(){
        String taskId = "28298a16-aa14-11ea-8d81-000ec6dd34b8";
        HistoricTaskInstance hti = historyService
                .createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();

        System.out.println("id：" +hti.getId());
        System.out.println("流程实例id：" +hti.getProcessInstanceId());
        System.out.println("任务定义key：" +hti.getTaskDefinitionKey());
        System.out.println("activityInstanceId：" +hti.getActivityInstanceId());
        System.out.println("流程定义ID：" +hti.getProcessDefinitionId());
        System.out.println("开始时间：" +hti.getStartTime());
        System.out.println("结束时间：" +hti.getEndTime());
    }

}
