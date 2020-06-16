package com.asarao;

import cn.hutool.core.util.RandomUtil;
import lombok.val;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Collection;

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

    @Test
    public void read() {

        File file = new File("C:\\projects\\spring-camunda\\src\\main\\resources\\processes\\simple-withdraw.bpmn");

        BpmnModelInstance modelInstance = Bpmn.readModelFromFile(file);

        Definitions definitions = modelInstance.getDefinitions();



//        // 当前任务节点 用户任务节点，非用户任务节点不加签
//        String activityId = "Activity_0mqumop";
//
//        BpmnModelInstance newModel = modelInstance.clone();
//        Definitions definitions = newModel.getDefinitions();
//
//
//        // 任务节点元素
//        UserTask userTask = newModel.getModelElementById(activityId);
//
//        // 父元素 Process
//        Process process = (Process) userTask.getParentElement();
//        String newProcessId = process.getId() + "_" + RandomUtil.randomString(7);
//        process.setId(newProcessId);
//        System.out.println(process.getElementType().getTypeName());
//
//        // 创建一个新的任务节点
//        String id = "Activity_" + RandomUtil.randomString(7);
//        UserTask newTask = createElement(process, id, UserTask.class);
//        newTask.setCamundaAssignee("${assignee}");
//
//        // 当前任务的输入序列(当前任务只有一个流入项)
//        Collection<SequenceFlow> incoming = userTask.getIncoming();
//        Iterator<SequenceFlow> iterator = incoming.iterator();
//        SequenceFlow sequenceFlow = iterator.next();
//        System.out.println(" 当前任务输入序列流ID" +sequenceFlow.getId());
//
//
//        definitions.removeChildElement(sequenceFlow);
//
//        // 源节点
//        FlowNode source = sequenceFlow.getSource();
//        createSequenceFlow(process,source,newTask);
//
//        // 创建新线
//        createSequenceFlow(process,newTask,userTask);
//
//        // 清除图形
//        definitions.removeAttribute("BPMNDiagram");
//
//        // validate and write model to file
//        Bpmn.validateModel(newModel);
//        String path = this.getClass().getResource("/").getPath();
//        System.out.println(path);
//        File newFile = new File(path + process.getId() + ".bpmn");
//        Bpmn.writeModelToFile(newFile, newModel);

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

    @Autowired
    RuntimeService runtimeService;

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
}