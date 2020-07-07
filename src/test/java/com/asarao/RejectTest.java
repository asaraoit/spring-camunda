package com.asarao;

import com.asarao.common.cmd.PreviousActivityCommand;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/*
 * @ClassName: RejectTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/7/3 15:44
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class RejectTest {

    @Autowired
    RepositoryService repositoryService;

    @Test
    public void deploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/reject.bpmn")
                .name("顺序签多实例")
                .deploy();
        System.out.println("部署成功");
    }

    @Autowired
    RuntimeService runtimeService;


    @Test
    public void startProcess(){
        String instanceKey = "reject";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(instanceKey);
        System.out.println("流程实例ID："+processInstance.getId());
        System.out.println("流程定义ID："+processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID："+processInstance.getProcessInstanceId());
    }

    @Autowired
    TaskService taskService;

    @Test
    public void completeTask(){
        String taskId = "fcde6bc8-bd36-11ea-8704-181deaf1ddd1";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Map<String,Object> variables = new HashMap<>(0);
        variables.put("approve","yes");
        taskService.complete(taskId,variables);
        System.out.println("任务完成");
    }

    @Test
    public void complete(){
        String taskId = "7e89921b-bd36-11ea-b06a-181deaf1ddd1";
        taskService.complete(taskId);
        System.out.println("任务完成");
    }

    @Autowired
    HistoryService historyService;

    @Test
    public void reject(){
        String taskId = "c160b210-bf2b-11ea-a581-181deaf1ddd1";
        Task task = taskService.createTaskQuery().taskId(taskId).active().singleResult();
        // 获取上一级UserTask
        TaskServiceImpl taskServiceImpl = (TaskServiceImpl) taskService;
        CommandExecutor executor = taskServiceImpl.getCommandExecutor();
//         上一级UserTask ID
        String previousActivity = executor.execute(new PreviousActivityCommand(taskId,historyService));
        System.out.println("上一任务节点ID：" + previousActivity);
        // 驳回
//        runtimeService.createProcessInstanceModification(task.getProcessInstanceId())
//                .cancelAllForActivity(task.getTaskDefinitionKey())
//                .startBeforeActivity(previousActivity)
//                .execute();
//        System.out.println("驳回成功");
    }

    @Autowired
    ManagementService managementService;


    @Test
    public void query(){
        List<HistoricActivityInstance> userTask = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId("7a8995c4-bd28-11ea-b030-181deaf1ddd1")
                .activityType("userTask")
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        for (HistoricActivityInstance historicActivityInstance : userTask) {
            HistoricActivityInstanceEntity userTaskEntity = (HistoricActivityInstanceEntity) historicActivityInstance;
            System.out.print("活动节点的ID：" + userTaskEntity.getActivityId());
            System.out.print("计数器："+userTaskEntity.getSequenceCounter());
            System.out.println("Name " +userTaskEntity.getActivityName());
            System.out.println("=========================");

        }
    }

    public static void main(String[] args) {
        String s1 = "initiateTask";     // A
        String s2 = "Activity_103hcmq"; // B
        String s3 = "Activity_14fzb68"; // E
        String s4 = "Activity_1dd9a6h"; // D
        String s5 = "Activity_14fzb68"; // E
        String s6 = "Activity_103hcmq"; // B
        String s7 = "initiateTask";     // A
        String s8 = "Activity_1s4xtmx"; // C
        String s9 = "Activity_1dd9a6h"; // D
        TreeSet<String> set = new TreeSet<>();
        set.add(s3);
        set.add(s8);
        int i = 1;
        for (String s : set) {
            System.out.println(i + "===" + s);
            i++;
        }
        System.out.println(set.last());
    }
}
