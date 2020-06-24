package com.asarao;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.GatewayDirection;
import org.camunda.bpm.model.bpmn.builder.*;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/*
 * @ClassName: ProcessTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/19 10:43
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessTest {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    FormService formService;

    @Test
    public void publish(){

        String processJson = "First JSONString dddd";

        // 当部署json发生变化--流程就得重新部署，但是流程ID不变

        String processId = "Process_kjd9bfz";
        if(StringUtils.isEmpty(processId)){
            processId = "Process_" + RandomUtil.randomString(7);
        }

        // 解析JSON 创建流程

        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess(processId)
                .startEvent()
                    .name("Start")
                .userTask()
                    .name("审批人")
                    .camundaAssignee("第一个办理人")
                .userTask()
                    .name("总经理审批")
                    .camundaAssignee("总经理")
                .userTask()
                    .name("财务审批")
                    .camundaAssignee("财务")
                .endEvent()
                    .name("End")
                .done();

        System.out.println(Bpmn.convertToString(modelInstance));

        repositoryService.createDeployment()
                .addModelInstance("test.bpmn", modelInstance)
                .addString("process_json",processJson)
                .addString("form","kkkk")
                .tenantId("company_id")
                .deploy();

    }

    @Test
    public void getForm(){

        String processDefinitionId = "Process_kjd9bfz:3:7c79f44e-b1dc-11ea-9c0f-000ec6dd34b8";

        // 获取启动流程的Form
        String startFormKey = formService.getStartFormKey(processDefinitionId);

        System.out.println(startFormKey);

        ProcessInstance businessKey = runtimeService.startProcessInstanceById(processDefinitionId, "businessKey");

        Map<String, Object> variables = new HashMap<>();
        runtimeService.startProcessInstanceById(processDefinitionId,"",variables);
    }

    @Test
    public void create(){
        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("invoice")
                .name("BPMN API Invoice Process")
                .startEvent()
                .name("Invoice received")
                .userTask()
                .name("Assign Approver")
                .camundaAssignee("demo")
                .userTask()
                .id("approveInvoice")
                .name("Approve Invoice")
                .exclusiveGateway()
                .name("Invoice approved?")
                .gatewayDirection(GatewayDirection.Diverging)
                .condition("yes", "${approved}")
                .userTask()
                .name("Prepare Bank Transfer")
                .camundaFormKey("embedded:app:forms/prepare-bank-transfer.html")
                .camundaCandidateGroups("accounting")
                .serviceTask()
                .name("Archive Invoice")
                .camundaClass("org.camunda.bpm.example.invoice.service.ArchiveInvoiceService")
                .endEvent()
                .name("Invoice processed")
                .moveToLastGateway()
                .condition("no", "${!approved}")
                .userTask()
                .name("Review Invoice")
                .camundaAssignee("demo")
                .exclusiveGateway()
                .name("Review successful?")
                .gatewayDirection(GatewayDirection.Diverging)
                .condition("no", "${!clarified}")
                .endEvent()
                .name("Invoice not processed")
                .moveToLastGateway()
                .condition("yes", "${clarified}")
                .connectTo("approveInvoice")
                .done();

        System.out.println(Bpmn.convertToString(modelInstance));
    }

    @Test
    public void create2(){
        String processDefinitionKey = "Process_" + RandomUtil.randomString(7);

        ProcessBuilder executableProcess = Bpmn.createExecutableProcess(processDefinitionKey);

        StartEventBuilder startEventBuilder = executableProcess.startEvent().name("开始");

        // 1. 发起任务
        UserTaskBuilder initial = startEventBuilder.userTask().name("提交申请").camundaAssignee("${assignee}");

        // 2. 判断字节点（若为排他网关）
        ExclusiveGatewayBuilder gatewayBuilder = initial
                .exclusiveGateway()
                .name("Selected Project")
                // 分叉
                .gatewayDirection(GatewayDirection.Diverging);
        // 条件节点 第一个子元素
        ExclusiveGatewayBuilder a = gatewayBuilder.condition("A", "${project=='A'}");
        // 假设下一个任务为多人的顺序签（A，B，C）
        UserTaskBuilder userTaskBuilder_A = a.userTask().name("A审批").camundaAssignee("A")
                .userTask().name("B审批").camundaAssignee("B")
                .userTask().name("C审批").camundaAssignee("C");
        // a线财务审批
        UserTaskBuilder userTaskBuilder_CA = userTaskBuilder_A.userTask().name("财务A审批").camundaAssignee("财务A");
        EndEventBuilder end_a = userTaskBuilder_CA.endEvent().name("End_A");

        // 判断是否还有子元素
        // 无  end_a.done();


        // b 线
        AbstractGatewayBuilder abstractGatewayBuilder = end_a.moveToLastGateway();
        AbstractFlowNodeBuilder b = abstractGatewayBuilder.condition("B", "${project=='B'}");
        AbstractActivityBuilder counterSignBuilder = b.userTask().name("会签").camundaAssignee("${assignee}")
                .multiInstance()
                .parallel()
                .camundaCollection("D,E,F")
                .camundaElementVariable("assignee")
                .completionCondition("${nrOfCompletedInstances==nrOfInstances}")
                .multiInstanceDone();
        UserTaskBuilder userTaskBuilder_CB = counterSignBuilder.userTask().name("财务B审批").camundaAssignee("财务B");
        EndEventBuilder end_b = userTaskBuilder_CB.endEvent().name("End_B");

        AbstractFlowNodeBuilder c = end_b.moveToLastGateway().condition("C", "${project=='C'}");
        AbstractActivityBuilder cBuilder = c.userTask().name("会签").camundaAssignee("${assignee}")
                .multiInstance()
                .parallel()
                .camundaCollection("G,H")
                .camundaElementVariable("assignee")
                .completionCondition("${nrOfCompletedInstances==1}")
                .multiInstanceDone();

        UserTaskBuilder userTaskBuilder_CC = cBuilder.userTask().name("财务C").camundaAssignee("财务C");
        EndEventBuilder end_c = userTaskBuilder_CC.endEvent().name("End_C");
        BpmnModelInstance done = end_c.done();


        System.out.println(Bpmn.convertToString(done));

        repositoryService.createDeployment()
                .addModelInstance("draw.bpmn", done)
                .deploy();
    }

    @Test
    public void start(){
        String key = "Process_0z8egm6";

        Map<String,Object> variables = new HashMap<>();
        variables.put("assignee","张三");
        runtimeService.startProcessInstanceByKey(key,variables);
        System.out.println("启动成功");
    }

    @Autowired
    TaskService taskService;

    @Test
    public void apply(){
        String taskId = "36d4b588-b606-11ea-9668-000ec6dd34b8";
        Map<String,Object> variables = new HashMap<>();
        variables.put("project","A");
        taskService.complete(taskId,variables);
        System.out.println("提交");
    }
}
