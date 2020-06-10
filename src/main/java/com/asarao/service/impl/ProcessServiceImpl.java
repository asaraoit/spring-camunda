package com.asarao.service.impl;

import com.asarao.service.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * @ClassName: ProcessServiceImpl
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/9 16:52
 * @Version: 1.0
 **/
@Service
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    /**
     *  委派
     *  当任务委派时，任务的所有者依旧是委派人，但是办理人变成了被委派人
     *@param taskId The id of the task that will be delegated.
     *@param userId The id of the user that will be set as assignee.
     */
    @Override
    public void delegate(String taskId, String userId) {
        taskService.delegateTask(taskId, userId);
        log.info("任务-{} 委派给了 [{}]", taskId, userId);
    }

    /**
     * 转办
     * @param taskId 任务ID
     * @param assignee 办理人
     */
    @Override
    public void turn(String taskId, String assignee) {
        taskService.setAssignee(taskId,assignee);
        log.info("任务-{} 转交给了 [{}]",taskId,assignee);
    }

    // 抢占
    @Override
    public void preemption(String taskId,String userId) {
        taskService.claim(taskId,userId);
    }

    // 会签
    @Override
    public void countersign() {
        /**
         * Multi Instance
         * Collection
         * Element variables
         * Completion Condition
         *      nrOfInstances
         *      nrOfCompletedInstances
         *      nrOfActiveInstances
         */
    }

    /**
     * 委托代办
     * @param taskId The id of the task that will be delegated.
     * @param userId The id of the user that will be set as assignee.
     *               此时原任务的办理人是此任务的owner
     */
    @Override
    public void concierge(String taskId,String userId) {
        taskService.delegateTask(taskId,userId);
    }

    @Autowired
    ManagementService managementService;

    // 催办
    @Override
    public void reminder() {

    }

    // 自由流
    @Override
    public void freeFlow() {

    }

    // 回退
    @Override
    public void fallback() {

    }

    // 取回
    @Override
    public void retrieve() {

    }


    /**
     * 指派
     *@param taskId id of the task, cannot be null.
     *@param userId id of the user to use as assignee.
     */
    @Override
    public void assign(String taskId,String userId) {
        taskService.setAssignee(taskId,userId);
    }


    /**
     * 前加签
     * 在当前任务的前面加签，如果选择此操作，则当前待办会消失，等待选择的加签人审批后才能办理当前任务
     */
    @Override
    public void beforeAddSign() {

    }


    /**
     * 后加签
     * 即在当前任务的后面加签，选择此操作后会将任务发送给选择的加签人审批，加签人审批后再发给流程设计的下一步人审批。
     */
    @Override
    public void afterAddSign() {

    }

    // 改派
    @Override
    public void reassign() {

    }

    // 驳回
    @Override
    public void reject() {

    }

    // 终止
    @Override
    public void terminate() {

    }

    // 挂起
    @Override
    public void hang() {
        String processInstanceId = "";
        runtimeService.suspendProcessInstanceById(processInstanceId);
        String processDefinitionId = "";
        runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);
        String processDefinitionKey = "";
        runtimeService.suspendProcessInstanceByProcessDefinitionKey(processDefinitionKey);
        // 改变流程实列的状态
        runtimeService.updateProcessInstanceSuspensionState()
                .byProcessInstanceId(processInstanceId)
//                .byProcessDefinitionKey()
//                .byProcessDefinitionId()
                .suspend();
//                .activate();

    }

    // 激活
    @Override
    public void active() {
        String processInstanceId = "";
        runtimeService.activateProcessInstanceById(processInstanceId);
        String processDefinitionId = "";
        runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);
        String processDefinitionKey = "";
        runtimeService.activateProcessInstanceByProcessDefinitionKey(processDefinitionKey);
    }
}
