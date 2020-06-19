package com.asarao;

import cn.hutool.core.util.RandomUtil;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
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
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to) {
        String identifier = "Flow_" + RandomUtil.randomString(7);
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
        final BpmnModelInstance myProcess = Bpmn.createExecutableProcess("process-payments")
                .startEvent()
                .serviceTask()
                .name("Process Payment")
                .endEvent()
                .done();

        System.out.println(Bpmn.convertToString(myProcess));
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
}
