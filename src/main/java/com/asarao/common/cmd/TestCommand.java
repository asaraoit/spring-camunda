package com.asarao.common.cmd;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.impl.type.ModelElementTypeImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/*
 * @ClassName: TestCommand
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/12 15:46
 * @Version: 1.0
 **/
@Slf4j
public class TestCommand implements Command<String> {

    private String taskId;

    private RepositoryService repositoryService;

    private RuntimeService runtimeService;

    public TestCommand(String taskId,RepositoryService repositoryService,
                       RuntimeService runtimeServicee){
        this.taskId = taskId;
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeServicee;
    }

    @Override
    public String execute(CommandContext commandContext) {

        // 获取任务
        TaskEntity currentTask = commandContext.getTaskManager().findTaskById(taskId);

        // 流程模型实列
        BpmnModelInstance bpmnModelInstance = currentTask.getBpmnModelInstance();

        BpmnModelInstance clone = bpmnModelInstance.clone();

        // 流程所有的定义
        Definitions definitions = clone.getDefinitions();

        // 定义中流程定义
        Collection<Process> processes = definitions.getChildElementsByType(Process.class);

        Iterator<Process> iterator = processes.iterator();

        Process process = iterator.next();
        process.setId(process.getId()+ "_" + RandomUtil.randomString(7));
        process.setName(process.getName());
        log.info("Process 的 ID:{} == NAME:{}",process.getId(),process.getName());

        Collection<FlowElement> flowElements = process.getFlowElements();

        // 这个新节点的源？
        ActivityImpl activity = currentTask.getProcessDefinition().findActivity(currentTask.getTaskDefinitionKey());
        PvmTransition pvmTransition = activity.getIncomingTransitions().get(0);
        PvmActivity source = pvmTransition.getSource();
        String sourceId = source.getId();
        log.info("当前任务节点的源节点：{}",sourceId);

        // 当前节点
        FlowElement currentFlowElement = null;
        // 源节点
        FlowElement sourceFlowElement = null;
        for (FlowElement flowElement:flowElements) {
            String flowElementId = flowElement.getId();
            if(flowElementId.equals(currentTask.getTaskDefinitionKey())){
                currentFlowElement = flowElement;
            }
            if(flowElementId.equals(sourceId)){
                sourceFlowElement = flowElement;
            }

            if(currentFlowElement!= null && sourceFlowElement != null){
                break;
            }
            log.info("FlowElement 的 ID:{} == NAME:{}", flowElementId,flowElement.getName());
        }

        Task task = (Task) currentFlowElement;
        Collection<SequenceFlow> incoming = task.getIncoming();
        // 当前任务的流入线
        SequenceFlow incomingFlow = incoming.iterator().next();
        task.removeChildElement(incomingFlow);
        definitions.removeChildElement(incomingFlow);
        Collection<BpmnDiagram> bpmDiagrams = definitions.getBpmDiagrams();
        for (BpmnDiagram bpmDiagram : bpmDiagrams) {
            definitions.removeChildElement(bpmDiagram);
        }

        // 新建节点
        String activityId = "activity_"+ RandomUtil.randomString(7);
        UserTask addTask = createElement(process, activityId, UserTask.class);
        addTask.setCamundaAssignee("我要你签");

        // 连线
        createSequenceFlow(process, (FlowNode) sourceFlowElement,addTask);
        // 前加签
        createSequenceFlow(process,addTask,task);

        // 删除原来的任务
        commandContext.getTaskManager().deleteTask(currentTask,null,false,true);
        currentTask.getExecution().removeTask(currentTask);

        Bpmn.validateModel(clone);
        File file = new File(process.getId() + ".bpmn");
        Bpmn.writeModelToFile(file, clone);

        publishProcess(process,process.getId()+".bpmn",clone);

        runtimeService.startProcessInstanceByKey(process.getId());
        return null;
    }

    private void publishProcess(Process process,String resourceName,BpmnModelInstance modelInstance) {
        Deployment deployment = repositoryService.createDeployment()
                .name(process.getId())
                .addModelInstance(resourceName, modelInstance)
                .deploy();
        System.out.println("流程部署ID: "+deployment.getId());
        System.out.println("流程部署NAME: "+deployment.getName());
        System.out.println("流程部署Source: "+deployment.getSource());
        System.out.println("流程部署TenantId: "+deployment.getTenantId());
        System.out.println("流程部署Time: "+deployment.getDeploymentTime());
    }


    private <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id,
                                                                   Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to) {
        String id = "Flow_" + RandomUtil.randomString(7);
        SequenceFlow sequenceFlow = createElement(process, id, SequenceFlow.class);
        process.addChildElement(sequenceFlow);
        sequenceFlow.setSource(from);
        from.getOutgoing().add(sequenceFlow);
        sequenceFlow.setTarget(to);
        to.getIncoming().add(sequenceFlow);
        return sequenceFlow;
    }
}
