package com.asarao;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.bpmn.builder.StartEventBuilder;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.camunda.bpm.model.bpmn.impl.instance.ExtensionElementsImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;


/*
 * @ClassName: BpmnTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/16 14:33
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class BpmnTest {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Test
    public void read() {

        File file = new File("C:\\projects\\spring-camunda\\src\\main\\resources\\processes\\simple-withdraw.bpmn");

        BpmnModelInstance modelInstance = Bpmn.readModelFromFile(file);


        BpmnModelInstance clone = modelInstance.clone();

        Definitions definitions = clone.getDefinitions();

        // 清除图形
        Collection<BpmnDiagram> bpmnDiagrams = definitions.getChildElementsByType(BpmnDiagram.class);
        for (BpmnDiagram bpmnDiagram : bpmnDiagrams) {
            definitions.removeChildElement(bpmnDiagram);
        }

        // 当前任务节点 用户任务节点，非用户任务节点不加签
        String activityId = "Activity_0mqumop";


        // 任务节点元素
        UserTask userTask = clone.getModelElementById(activityId);

        // 父元素 Process
        Process process = (Process) userTask.getParentElement();
        String newProcessId = process.getId() + "_" + RandomUtil.randomString(7);
        process.setId(newProcessId);
        System.out.println(process.getElementType().getTypeName());

        // 创建一个新的任务节点
        String id = "Activity_" + RandomUtil.randomString(7);
        UserTask newTask = createElement(process, id, UserTask.class);
        newTask.setCamundaAssignee("${assignee}");

        // 当前任务的输入序列(当前任务只有一个流入项)
        Collection<SequenceFlow> incoming = userTask.getIncoming();
        Iterator<SequenceFlow> iterator = incoming.iterator();
        SequenceFlow sequenceFlow = iterator.next();
        System.out.println(" 当前任务输入序列流ID" +sequenceFlow.getId());


        definitions.removeChildElement(sequenceFlow);

        // 源节点
        FlowNode source = sequenceFlow.getSource();
        createSequenceFlow(process,source,newTask);

        // 创建新线
        createSequenceFlow(process,newTask,userTask);

        System.out.println(Bpmn.convertToString(clone));



//        Deployment deploy = repositoryService.createDeployment()
//                .addModelInstance(process.getId(), clone)
//                .name("加签部署")
//                .deploy();


    }


    private <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        if(StrUtil.isEmpty(id)){
            id = elementClass.getSimpleName() + IdUtil.simpleUUID();
        }
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to) {
        String identifier = "Flow_" + IdUtil.simpleUUID();
        SequenceFlow sequenceFlow = createElement(process, identifier, SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);
        return sequenceFlow;
    }

    @Test
    public void fluent(){
//        final BpmnModelInstance myProcess = Bpmn.createExecutableProcess("process-payments")
//                .startEvent()
//                    .userTask()
//                    .name("Process Payment")
//                .endEvent()
//                .done();

        ProcessBuilder processBuilder = Bpmn.createExecutableProcess("process-test").name("1");

        StartEventBuilder startEventBuilder = processBuilder.startEvent().name("start");

        UserTaskBuilder userTaskBuilder = startEventBuilder.userTask().name("用户任务");


        UserTask element = userTaskBuilder.getElement();
        ExtensionElements extensionElements = element.getModelInstance().newInstance(ExtensionElements.class);
        element.setExtensionElements(extensionElements);

        // property
        CamundaProperties camundaProperties = extensionElements.addExtensionElement(CamundaProperties.class);
        CamundaProperty camundaProperty = camundaProperties.getModelInstance().newInstance(CamundaProperty.class);
        camundaProperty.setCamundaName("copyUsers");
        camundaProperty.setCamundaValue("A,B");
        camundaProperties.addChildElement(camundaProperty);

        // listener
        CamundaTaskListener camundaTaskListener = extensionElements.addExtensionElement(CamundaTaskListener.class);
        camundaTaskListener.setCamundaEvent("complete");
        camundaTaskListener.setCamundaDelegateExpression("#{completeListener}");

        BpmnModelInstance modelInstance = userTaskBuilder.endEvent().name("End").done();


        System.out.println(Bpmn.convertToString(modelInstance));
    }

    @Test
    public void migration(){
        MigrationPlan migrationPlan = runtimeService
                .createMigrationPlan("exampleProcess:1", "exampleProcess:2")
                .mapActivities("assessCreditWorthiness", "assessCreditWorthiness")
                .mapActivities("validateAddress", "validatePostalAddress")
                .mapActivities("archiveApplication", "archiveApplication")
                .mapEqualActivities()
                .build();

    }

    @Test
    public void create(){
        /**
         * <bpmn:definitions
         *  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
         *  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
         *  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
         *  xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
         *  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
         *  id="Definitions_1v13f4o"
         *  targetNamespace="http://bpmn.io/schema/bpmn"
         *  exporter="Camunda Modeler"
         *  exporterVersion="4.0.0">
         */

        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();


        Definitions definitions = modelInstance.newInstance(Definitions.class);

        definitions.getDomElement().registerNamespace("bpmn", "http://www.omg.org/spec/BPMN/20100524/MODEL");
        definitions.getDomElement().registerNamespace("bpmndi","http://www.omg.org/spec/BPMN/20100524/DI");
        definitions.getDomElement().registerNamespace("dc","http://www.omg.org/spec/DD/20100524/DC");
        definitions.getDomElement().registerNamespace("camunda","http://camunda.org/schema/1.0/bpmn");
        definitions.getDomElement().registerNamespace("di","http://www.omg.org/spec/DD/20100524/DI");

        definitions.setTargetNamespace("http://bpmn.io/schema/bpmn");

        definitions.setId("Definitions_1v13f4o");
        definitions.setExporter("Camunda Modeler");
        definitions.setExporterVersion("4.0.0");



        modelInstance.setDefinitions(definitions);
        Process process = modelInstance.newInstance(Process.class);
        definitions.addChildElement(process);

        BpmnDiagram bpmnDiagram = modelInstance.newInstance(BpmnDiagram.class);

        BpmnPlane bpmnPlane = modelInstance.newInstance(BpmnPlane.class);
        bpmnPlane.setBpmnElement(process);

        bpmnDiagram.addChildElement(bpmnPlane);
        definitions.addChildElement(bpmnDiagram);

        System.out.println(Bpmn.convertToString(modelInstance));

    }

    @Test
    public void draw(){

        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();

        Definitions definitions = modelInstance.newInstance(Definitions.class);

        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        // create the process
        String processDefinitionKey = "process-test";
        Process process = createElement(definitions, processDefinitionKey, Process.class);
        process.setExecutable(true);

        // startEvent
        StartEvent startEvent = createElement(process, null, StartEvent.class);
        startEvent.setName("start");

        // submitTask
        UserTask submit = createElement(process, null, UserTask.class);
        submit.setName("提交申请");
        submit.setCamundaAssignee("#{assignee}");

        // ExclusiveGateway
        ExclusiveGateway exclusiveGateway = createElement(process, null, ExclusiveGateway.class);
        exclusiveGateway.setName("fork");

        createSequenceFlow(process,submit,exclusiveGateway);

        // sequential UserTask
        UserTask sequentialUserTask = createElement(process, null, UserTask.class);
        sequentialUserTask.setName("sequential userTask");
        MultiInstanceLoopCharacteristics sequential = createElement(sequentialUserTask, null, MultiInstanceLoopCharacteristics.class);
        sequential.setSequential(true);
        sequential.setCamundaCollection("assigneeList");
        sequential.setCamundaElementVariable("assignee");
        SequenceFlow flow_A = createSequenceFlow(process, exclusiveGateway, sequentialUserTask);
        ConditionExpression c_A = modelInstance.newInstance(ConditionExpression.class);
        c_A.setCamundaResource("#{project=='A'}");
        flow_A.setConditionExpression(c_A);

        // approveTask A
        UserTask approve = createElement(process, null, UserTask.class);
        approve.setName("财务审批");
        approve.setCamundaAssignee("CA");
        createSequenceFlow(process,sequentialUserTask,approve);

        // EndEvent_A
        EndEvent end_A = createElement(process, null, EndEvent.class);
        end_A.setName("end_A");
        createSequenceFlow(process,approve,end_A);

        // parallel UserTask
        UserTask parallelUserTask = createElement(process, null, UserTask.class);
        parallelUserTask.setName("parallel UserTask");
        parallelUserTask.setCamundaAssignee("#{assignee}");
        MultiInstanceLoopCharacteristics parallel = createElement(parallelUserTask, null, MultiInstanceLoopCharacteristics.class);
        parallel.setSequential(false);
        parallel.setCamundaCollection("assigneeList");
        parallel.setCamundaElementVariable("assignee");
        SequenceFlow flow_B = createSequenceFlow(process, exclusiveGateway, parallelUserTask);
        ConditionExpression c_B = modelInstance.newInstance(ConditionExpression.class);
        c_B.setType("");
        c_B.setTextContent("#{project=='B'}");
        flow_A.setConditionExpression(c_B);



        // approveTask B
        UserTask approve_B = createElement(process, null, UserTask.class);
        approve_B.setName("财务审批");
        approve_B.setCamundaAssignee("CB");
        createSequenceFlow(process,parallelUserTask,approve_B);

        // EndEvent_B
        EndEvent end_B = createElement(process, null, EndEvent.class);
        end_B.setName("end_B");
        createSequenceFlow(process,approve_B,end_B);


        // or UserTask
        UserTask orUserTask = createElement(process, null, UserTask.class);
        orUserTask.setName("orUserTask");
        orUserTask.setCamundaAssignee("#{assignee}");
        MultiInstanceLoopCharacteristics or = createElement(orUserTask, null, MultiInstanceLoopCharacteristics.class);
        or.setSequential(false);
        or.setCamundaCollection("assigneeList");
        or.setCamundaElementVariable("assignee");
        CompletionCondition completionCondition = modelInstance.newInstance(CompletionCondition.class);
        completionCondition.setTextContent("#{nrOfCompletedInstances==1}");
        or.setCompletionCondition(completionCondition);
        SequenceFlow flow_C = createSequenceFlow(process, exclusiveGateway, orUserTask);
        exclusiveGateway.setDefault(flow_C);


        // approveTask C
        UserTask approve_C = createElement(process, null, UserTask.class);
        approve_C.setName("财务审批");
        approve_C.setCamundaAssignee("CC");
        createSequenceFlow(process,orUserTask,approve_C);

        // EndEvent_C
        EndEvent end_C = createElement(process, null, EndEvent.class);
        end_C.setName("end_B");
        createSequenceFlow(process,approve_C,end_C);


        System.out.println(Bpmn.convertToString(modelInstance));

    }

}
