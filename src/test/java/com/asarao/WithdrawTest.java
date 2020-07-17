package com.asarao;

import com.asarao.common.OSSConstants;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Value("${spring.application.name}")
    private String config;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Test
    public void deploy() {
//        repositoryService.createDeployment()
//                .addClasspathResource("processes/countersign.bpmn")
//                .name("测试取回")
//                .deploy();
//        System.out.println("部署成功");
        System.out.println(config);
        System.out.println(OSSConstants.END_POINT);
    }

    @Test
    public void startProcess() {
        String instanceKey = "countersign";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey);
        System.out.println("流程实例ID：" + processInstance.getId());
        System.out.println("流程实例业务Key：" + processInstance.getBusinessKey());
        System.out.println("流程实例CaseInstanceId：" + processInstance.getCaseInstanceId());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID：" + processInstance.getProcessInstanceId());
        System.out.println("流程实例TenantID：" + processInstance.getTenantId());
        System.out.println("流程实例is Suspended：" + processInstance.isSuspended());
    }

    // 查询任务（根据办理人）
    @Test
    public void searchTask() {
        String assignee = "A";
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        if (null != tasks && !tasks.isEmpty()) {
            for (Task task : tasks) {
                System.out.println("待办任务ID: " + task.getId());
                System.out.println("待办任务定义key: " + task.getTaskDefinitionKey());
                System.out.println("流程实例ID: " + task.getProcessInstanceId());
                System.out.println("流程定义ID: " + task.getProcessDefinitionId());
                System.out.println("待办任务name: " + task.getName());
            }
        }
    }

    @Test
    public void completeTask() {
        String taskId = "d113ad96-af7a-11ea-b0a2-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("任务：[" + taskId + "] 完成");
    }

    @Autowired
    HistoryService historyService;

    @Test
    public void getHistoricTaskById() {
        String taskId = "00c5242b-aed8-11ea-aeb6-000ec6dd34b8";
        HistoricTaskInstance hti = historyService
                .createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();

        System.out.println("id：" + hti.getId());
        System.out.println("流程实例id：" + hti.getProcessInstanceId());
        System.out.println("任务定义key：" + hti.getTaskDefinitionKey());
        System.out.println("activityInstanceId：" + hti.getActivityInstanceId());
        System.out.println("流程定义ID：" + hti.getProcessDefinitionId());
        System.out.println("开始时间：" + hti.getStartTime());
        System.out.println("结束时间：" + hti.getEndTime());
    }

    @Test
    public void withdraw() {

        // 历史任务
        String taskId = "6f0c61a2-aed9-11ea-95ab-000ec6dd34b8";

        // 历史任务
        HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId).singleResult();
        System.out.println("历史任务ID：" + historicTask.getId());

        // 取得流程
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(historicTask.getProcessInstanceId()).singleResult();
        System.out.println("流程ID：" + processInstance.getId());

        // 运行时变量
        Map<String, Object> variables = runtimeService.getVariables(historicTask.getExecutionId());

        // 流程定义
        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)
                repositoryService.getProcessDefinition(historicTask.getProcessDefinitionId());
        // 历史任务的活动节点
        ActivityImpl hisActivity = definitionEntity.findActivity(historicTask.getTaskDefinitionKey());
        System.out.println(hisActivity.getActivityId());

        // 任务的流向
        List<PvmTransition> outgoingTransitions = hisActivity.getOutgoingTransitions();
        System.out.println(outgoingTransitions);
        for (PvmTransition transition : outgoingTransitions) {
            // 当前任务节点
            PvmActivity currentActivity = transition.getDestination();
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(historicTask.getProcessInstanceId())
                    .taskDefinitionKey(currentActivity.getId()).list();
            for (Task task : tasks) {
                List<PvmTransition> pvmTransitionList = currentActivity.getOutgoingTransitions();
                List<PvmTransition> oriPvmTransitionList = new ArrayList<>(pvmTransitionList);
                pvmTransitionList.clear();
                System.out.println(oriPvmTransitionList);

                // 建立新方向
                ActivityImpl nextActivityImpl = definitionEntity.findActivity(currentActivity.getId());
                TransitionImpl newTransition = nextActivityImpl.createOutgoingTransition();
                newTransition.setDestination(hisActivity);

                taskService.claim(task.getId(), null);
                taskService.complete(task.getId(), variables);
                historyService.deleteHistoricTaskInstance(task.getId());

                hisActivity.getIncomingTransitions().remove(newTransition);
                List<PvmTransition> pvmTList = currentActivity.getOutgoingTransitions();
                pvmTList.clear();
                pvmTransitionList.addAll(oriPvmTransitionList);
                System.out.println(pvmTransitionList);
            }
        }
        historyService.deleteHistoricTaskInstance(historicTask.getId());
        System.out.println("撤回完成");
    }

    @Test
    public void withdraw2() {

        // 历史任务
        String taskId = "9e8090e5-aede-11ea-bc17-000ec6dd34b8";

        // 历史任务
        HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId).singleResult();
        System.out.println("历史任务ID：" + historicTask.getId());

        // 取得流程
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(historicTask.getProcessInstanceId()).singleResult();
        System.out.println("流程ID：" + processInstance.getId());

        // 运行时变量
        Map<String, Object> variables = runtimeService.getVariables(historicTask.getExecutionId());

        // 流程定义
        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity)
                repositoryService.getProcessDefinition(historicTask.getProcessDefinitionId());
        // 历史任务的活动节点
        ActivityImpl hisActivity = definitionEntity.findActivity(historicTask.getTaskDefinitionKey());
        System.out.println(hisActivity.getActivityId());

        // 任务的流向
        List<PvmTransition> outgoingTransitions = hisActivity.getOutgoingTransitions();
        System.out.println(outgoingTransitions);
        for (PvmTransition transition : outgoingTransitions) {
            // 当前任务节点
            PvmActivity currentActivity = transition.getDestination();
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(historicTask.getProcessInstanceId())
                    .taskDefinitionKey(currentActivity.getId()).list();
            if(tasks.isEmpty()){
                // 当前节点为非任务节点
                List<PvmTransition> pvmTransitionList = currentActivity.getOutgoingTransitions();
                List<PvmTransition> newPvmTransitions = new ArrayList<>(pvmTransitionList);
                pvmTransitionList.clear();

                ActivityImpl gateway = definitionEntity.findActivity(currentActivity.getId());
                TransitionImpl toTarget = gateway.createOutgoingTransition();
                toTarget.setDestination(hisActivity);



                for (PvmTransition pvmTransition:newPvmTransitions) {
                    PvmActivity destination = pvmTransition.getDestination();
                    System.out.println(destination);
                    List<Task> nextTasks = taskService.createTaskQuery()
                            .processInstanceId(historicTask.getProcessInstanceId())
                            .taskDefinitionKey(destination.getId()).list();
                    for (Task task : nextTasks) {
                        List<PvmTransition> nexPvmTransitionList = destination.getOutgoingTransitions();
                        List<PvmTransition> oriPvmTransitionList = new ArrayList<>(nexPvmTransitionList);
                        pvmTransitionList.clear();
                        System.out.println(oriPvmTransitionList);

                        // 建立新方向
                        ActivityImpl nextActivityImpl = definitionEntity.findActivity(destination.getId());
                        TransitionImpl newTransition = nextActivityImpl.createOutgoingTransition();
                        newTransition.setDestination((ActivityImpl) currentActivity);

                        taskService.claim(task.getId(), null);
                        taskService.complete(task.getId(), variables);
                        historyService.deleteHistoricTaskInstance(task.getId());

                        currentActivity.getIncomingTransitions().remove(newTransition);
                        List<PvmTransition> pvmTList = destination.getOutgoingTransitions();
                        pvmTList.clear();
                        pvmTransitionList.addAll(oriPvmTransitionList);
                        System.out.println(pvmTransitionList);
                    }
                }

                hisActivity.getIncomingTransitions().remove(toTarget);
                List<PvmTransition> pvmTList = currentActivity.getOutgoingTransitions();
                pvmTList.clear();
                pvmTransitionList.addAll(newPvmTransitions);
                System.out.println(pvmTransitionList);

            }else {
                for (Task task : tasks) {
                    List<PvmTransition> pvmTransitionList = currentActivity.getOutgoingTransitions();
                    List<PvmTransition> oriPvmTransitionList = new ArrayList<>(pvmTransitionList);
                    pvmTransitionList.clear();
                    System.out.println(oriPvmTransitionList);

                    // 建立新方向
                    ActivityImpl nextActivityImpl = definitionEntity.findActivity(currentActivity.getId());
                    TransitionImpl newTransition = nextActivityImpl.createOutgoingTransition();
                    newTransition.setDestination(hisActivity);

                    taskService.claim(task.getId(), null);
                    taskService.complete(task.getId(), variables);
                    historyService.deleteHistoricTaskInstance(task.getId());

                    hisActivity.getIncomingTransitions().remove(newTransition);
                    List<PvmTransition> pvmTList = currentActivity.getOutgoingTransitions();
                    pvmTList.clear();
                    pvmTransitionList.addAll(oriPvmTransitionList);
                    System.out.println(pvmTransitionList);
                }
            }

        }
        historyService.deleteHistoricTaskInstance(historicTask.getId());
        System.out.println("撤回完成");
    }


    @Test
    public void modification(){

        String taskId = "e730f717-af7d-11ea-9e4b-000ec6dd34b8";

        // 历史任务
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId).singleResult();

        // 流程实例ID
        String processInstanceId = historicTaskInstance.getProcessInstanceId();

        // 流程定义实体
//        ProcessDefinitionEntity processDefinitionEntity =
//                (ProcessDefinitionEntity) repositoryService
//                        .getProcessDefinition(historicTaskInstance.getProcessDefinitionId());
//
//        ActivityImpl activity = processDefinitionEntity.findActivity(historicTaskInstance.getTaskDefinitionKey());
//
//        List<PvmTransition> outgoingTransitions = activity.getOutgoingTransitions();


        runtimeService.createProcessInstanceModification(processInstanceId)
                .cancelAllForActivity("Activity_0xrswpv")
                .startBeforeActivity("Activity_0ionylq")
                .execute();

        System.out.println("成功");
    }
}
