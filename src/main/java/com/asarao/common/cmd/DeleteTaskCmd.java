package com.asarao.common.cmd;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;

/*
 * @ClassName: DeleteTaskCmd
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/11 14:28
 * @Version: 1.0
 **/
public class DeleteTaskCmd implements Command<String> {

    private String taskId;

    public DeleteTaskCmd(String taskId){
        this.taskId = taskId;
    }

    @Override
    public String execute(CommandContext commandContext) {
        TaskManager taskManager = commandContext.getTaskManager();
        TaskEntity currentTask = taskManager.findTaskById(taskId);
        ExecutionEntity executionEntity = currentTask.getExecution();
        taskManager.deleteTask(currentTask,"skipReason",false,true);
        return executionEntity.getId();
    }
}
