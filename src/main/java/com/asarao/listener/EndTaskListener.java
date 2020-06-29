package com.asarao.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * @ClassName: EndTaskListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/29 15:18
 * @Version: 1.0
 **/
@Component
@Slf4j
public class EndTaskListener implements ExecutionListener {

    @Autowired
    TaskService taskService;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        List copyUsers = (List) execution.getVariable("copyUsers");
        Task delegateTask = taskService.createTaskQuery().executionId(execution.getId()).singleResult();
        if(copyUsers!= null && !copyUsers.isEmpty()){
            for (Object copyUser : copyUsers) {
                String assigneeUser = (String) copyUser;
                Task task = taskService.newTask();
                task.setParentTaskId(delegateTask.getId());
                task.setAssignee(assigneeUser);
                taskService.saveTask(task);
                log.info("[抄送给：{}]的任务创建成功",assigneeUser);
                log.info("抄送任务：{}",task);
                taskService.complete(task.getId());
            }
        }

    }
}
