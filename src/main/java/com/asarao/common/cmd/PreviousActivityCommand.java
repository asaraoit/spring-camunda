package com.asarao.common.cmd;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.*;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.util.*;

/*
 * @ClassName: PreviousActivityCommand
 * @Description: 获取节点的上一级节点（非网关）
 * @Author: Asarao
 * @Date: 2020/6/24 14:04
 * @Version: 1.0
 **/
@Slf4j
public class PreviousActivityCommand implements Command<String> {

    private String taskId;

    private HistoryService historyService;

    public PreviousActivityCommand(String taskId,HistoryService historyService){
        this.taskId = taskId;
        this.historyService = historyService;
    }

    @Override
    public String execute(CommandContext commandContext) {

        TaskManager taskManager = commandContext.getTaskManager();

        TaskEntity currentTask = taskManager.findTaskById(taskId);
        if(currentTask == null){
            return null;
        }
        String taskDefinitionKey = currentTask.getTaskDefinitionKey();
        if("initiateTask".equals(taskDefinitionKey)){
            return null;
        }
        UserTask elementInstance = currentTask.getBpmnModelElementInstance();
        Collection<SequenceFlow> outgoing = elementInstance.getOutgoing();
//        SequenceFlow sequenceFlow = elementInstance.getOutgoing().iterator().next();
        List<String> lasts = new ArrayList<>();
        getLastUserTask(lasts, outgoing);
        System.out.println(lasts);

//        String nextUserTaskId = sequenceFlow.getTarget().getId();

        List<HistoricActivityInstance> userTask = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(currentTask.getProcessInstanceId())
                .activityType("userTask")
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        List<String> list = new ArrayList<>();
        for (int i = 0; i < userTask.size(); i++) {
            String activityId = userTask.get(i).getActivityId();
            log.info("节点：{}",activityId);
            if(taskDefinitionKey.equals(activityId)){
                String pre = userTask.get(i - 1).getActivityId();
                if(!lasts.contains(pre)){
                   list.add(pre);
                }
//                if(!pre.equals(nextUserTaskId)){
//                    list.add(pre);
//                }
            }
        }
        if(!list.isEmpty()){
            return list.get(list.size() -1);
        }
        return null;
    }

    private List<String> getLastUserTask(List<String> lasts,Collection<SequenceFlow> outgoing){
        for (SequenceFlow sequenceFlow : outgoing) {
            FlowNode target = sequenceFlow.getTarget();
            String typeName = target.getElementType().getTypeName();
            if("userTask".equalsIgnoreCase(typeName)){
                lasts.add(target.getId());
            }else {
                Collection<SequenceFlow> targetOutgoing = target.getOutgoing();
                return getLastUserTask(lasts,targetOutgoing);
            }
        }
        return lasts;
    }
}
