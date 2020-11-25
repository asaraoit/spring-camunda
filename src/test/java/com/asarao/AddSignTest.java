package com.asarao;

import com.asarao.common.cmd.PreviousActivityCommand;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName: AddSignTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/11/25 16:28
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddSignTest {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/add_sign.bpmn")
                .name("加签")
                .deploy();
        System.out.println("部署成功");
        runtimeService.startProcessInstanceByKey("add_sign");
        System.out.println("启动成功");
    }

    @Test
    public void addSign(){
        String taskId = "bb7e06d0-2f06-11eb-8d50-181deaf1ddd1";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/add_sign_2.bpmn")
                .name("add_sign_2")
                .deploy();
        System.out.println("部署成功");
        ProcessDefinition targetProcessDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("add_sign_2")
                .singleResult();


        // 创建迁移计划
        String sourceProcessDefinitionId = task.getProcessDefinitionId();
        String targetProcessDefinitionId = targetProcessDefinition.getId();
        MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinitionId,targetProcessDefinitionId)
                .mapEqualActivities()
                .build();

        // 在指定的流程实例上执行迁移计划
        runtimeService.newMigration(migrationPlan)
                .processInstanceIds(task.getProcessInstanceId())
                .skipCustomListeners()
                .skipIoMappings()
                .execute();
        System.out.println("迁移完成");

        taskService.complete(taskId);
        System.out.println("完成任务");

    }

    @Test
    public void addSignBefroe(){
        String taskId = "a18e98c0-2f09-11eb-9986-181deaf1ddd1";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/add_sign_before.bpmn")
                .name("add_sign_before")
                .deploy();
        System.out.println("部署成功");
        ProcessDefinition targetProcessDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("add_sign_before")
                .singleResult();


        // 创建迁移计划
        String sourceProcessDefinitionId = task.getProcessDefinitionId();
        String targetProcessDefinitionId = targetProcessDefinition.getId();
        MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinitionId,targetProcessDefinitionId)
                .mapEqualActivities()
                .build();

        // 在指定的流程实例上执行迁移计划
        runtimeService.newMigration(migrationPlan)
                .processInstanceIds(task.getProcessInstanceId())
                .skipCustomListeners()
                .skipIoMappings()
                .execute();
        System.out.println("迁移完成");

        // 驳回
        String previousActivity = "Activity_0wg1gs2";
        runtimeService.createProcessInstanceModification(task.getProcessInstanceId())
                .cancelAllForActivity(task.getTaskDefinitionKey())
                .startBeforeActivity(previousActivity)
                .execute();
        System.out.println("前加签完成");

    }
}
