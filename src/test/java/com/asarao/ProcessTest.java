package com.asarao;

import cn.hutool.core.util.RandomUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.GatewayDirection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}
