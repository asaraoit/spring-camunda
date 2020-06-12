package com.asarao;

import com.asarao.common.cmd.AddSignOnBeforeCommand;
import com.asarao.common.cmd.TestCommand;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
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
    public void countersign(){
        String taskId = "01dbccc2-ac7e-11ea-b94e-000ec6dd34b8";
        TaskServiceImpl taskServiceIpm = (TaskServiceImpl) taskService;
        CommandExecutor commandExecutor = taskServiceIpm.getCommandExecutor();
//        commandExecutor.execute(new SequentialCountersignCmd(taskId,"",true));
        commandExecutor.execute(new AddSignOnBeforeCommand(taskId,null,null));
    }

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Test
    public void cmd(){
        String taskId = "95e7f5e7-ac8b-11ea-aac5-000ec6dd34b8";
        TaskServiceImpl taskServiceIpm = (TaskServiceImpl) taskService;
        CommandExecutor commandExecutor = taskServiceIpm.getCommandExecutor();
        commandExecutor.execute(new TestCommand(taskId,repositoryService,runtimeService));
    }


    @Test
    public void complete(){
        String taskId = "89bcd825-ac7f-11ea-9b0b-000ec6dd34b8";
        taskService.complete(taskId);
        System.out.println("任务完成");
    }
}
