package com.asarao;

import com.asarao.common.cmd.AddSignOnBeforeCommand;
import com.asarao.common.cmd.JumpActivityCmd;
import com.asarao.common.cmd.TestCommand;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * @ClassName: CmdTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/12 10:15
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class CmdTest {


    @Autowired
    TaskService taskService;

    @Test
    public void countersign() {
        String taskId = "01dbccc2-ac7e-11ea-b94e-000ec6dd34b8";
        TaskServiceImpl taskServiceIpm = (TaskServiceImpl) taskService;
        CommandExecutor commandExecutor = taskServiceIpm.getCommandExecutor();
//        commandExecutor.execute(new SequentialCountersignCmd(taskId,"",true));
        commandExecutor.execute(new AddSignOnBeforeCommand(taskId, null, null));
    }

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Test
    public void cmd() {
        String taskId = "95e7f5e7-ac8b-11ea-aac5-000ec6dd34b8";
        TaskServiceImpl taskServiceIpm = (TaskServiceImpl) taskService;
        CommandExecutor commandExecutor = taskServiceIpm.getCommandExecutor();
        commandExecutor.execute(new TestCommand(taskId, repositoryService, runtimeService));
    }


    @Test
    public void complete() {
        String taskId = "89bcd825-ac7f-11ea-9b0b-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("任务完成");
    }

    @Autowired
    HistoryService historyService;

    @Autowired
    ManagementService managementService;

    @Test
    public void jump() throws Exception {
        String taskId = "7099fde1-aee7-11ea-b458-000ec6dd34b8";
        String activityId = "Activity_0mqumop";
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId).finished().singleResult();
        if (historicTaskInstance != null) {
            throw new Exception("任务已结束，不能进行回退操作！");
        }
//        if (activityId == null || "".equals(activityId)) {
//            throw new Exception("回退目标节点不能为空！");
//        }
        long count = taskService.createTaskQuery().taskId(taskId).count();
        if (count == 0) {
            throw new Exception("要驳回的任务已不存在！");
        }

        Task currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getProcessDefinition(currentTask.getProcessDefinitionId());
        String executionId = currentTask.getExecutionId();
        ActivityImpl activityImpl = definitionEntity.findActivity(activityId);
        if (activityImpl == null) {
            throw new Exception("要驳回的节点不存在！");
        }

        ManagementServiceImpl managementServiceImpl = (ManagementServiceImpl) managementService;
        CommandExecutor commandExecutor = managementServiceImpl.getCommandExecutor();
        commandExecutor.execute(new JumpActivityCmd(activityId, executionId,taskId));
        System.out.println("撤回完成");
    }
}
