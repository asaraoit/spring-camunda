package com.asarao.common.behavior;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * @ClassName: NotifyTask
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/7/7 10:33
 * @Version: 1.0
 **/
@Component
@Slf4j
public class NotifyTask implements JavaDelegate {

    private FixedValue notifyUsers;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String usersStr = notifyUsers.getExpressionText();
        log.info("抄送人员：{}",usersStr);
        // 随便获取一个当前流程的运行任务作为父任务
        TaskService taskService = execution.getProcessEngineServices().getTaskService();
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(execution.getProcessInstanceId())
                .active()
                .list();
        if(tasks == null || tasks.isEmpty()){
            return;
        }
        String[] users = usersStr.split(",");
        for (String user : users) {
            Task task = taskService.newTask();
            task.setParentTaskId(tasks.get(0).getId());
            task.setAssignee(user);
            task.setDescription("抄送任务");
            TaskEntity taskEntity = (TaskEntity) task;
            // 设置流程ID，查询的时候会用到
            taskEntity.setProcessInstanceId(execution.getProcessInstanceId());
            taskService.saveTask(taskEntity);
            // 直接完成抄送任务
            taskService.complete(task.getId());
        }
        log.info("抄送完成");
    }

    public FixedValue getNotifyUsers() {
        return notifyUsers;
    }

    public void setNotifyUsers(FixedValue notifyUsers) {
        this.notifyUsers = notifyUsers;
    }
}
