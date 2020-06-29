package com.asarao.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * @ClassName: CompleteTaskListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/29 15:00
 * @Version: 1.0
 **/
@Component
@Slf4j
public class CompleteTaskListener implements TaskListener {

    @Autowired
    TaskService taskService;

    @Override
    public void notify(DelegateTask delegateTask) {
        List copyUsers = (List) delegateTask.getVariable("copyUsers");
        if(copyUsers!= null && !copyUsers.isEmpty()){
            for (Object copyUser : copyUsers) {
                String assigneeUser = (String) copyUser;
                Task task = taskService.newTask();
                task.setParentTaskId(delegateTask.getId());
                task.setAssignee(assigneeUser);
                taskService.saveTask(task);
                log.info("[抄送给：{}]的任务创建成功",assigneeUser);
            }
        }
    }
}
