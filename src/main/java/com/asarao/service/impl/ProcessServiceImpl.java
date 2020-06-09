package com.asarao.service.impl;

import com.asarao.service.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
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
     *
     */
    @Override
    public void delegate(String taskId, String userId) {
        /**
         *@param taskId The id of the task that will be delegated.
         *@param userId The id of the user that will be set as assignee.
         */
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
    public void preemption() {
    }

    // 会签
    @Override
    public void countersign() {

    }

    // 委托代办
    @Override
    public void concierge() {

    }

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

    // 指派
    @Override
    public void assign() {

    }

    // 前加签
    @Override
    public void beforeAddSign() {

    }

    // 后加签
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

    }

    // 激活
    @Override
    public void active() {

    }
}
