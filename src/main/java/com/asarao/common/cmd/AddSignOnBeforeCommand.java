package com.asarao.common.cmd;

import cn.hutool.core.util.RandomUtil;
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.impl.core.delegate.CoreActivityBehavior;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * @ClassName: AddSignOnBeforeCommand
 * @Description: 前加签
 * @Author: Asarao
 * @Date: 2020/6/12 13:21
 * @Version: 1.0
 **/
public class AddSignOnBeforeCommand implements Command<String> {

    private String taskId;

    private String assignee;

    private Map<String,Object> variables;

    public AddSignOnBeforeCommand(String taskId,String assignee,Map<String,Object> variables){
        this.taskId = taskId;
        this.assignee = assignee;
        this.variables = variables;
    }

    /**
     * 前加签
     *      获取当前的任务
     *      当前任务的流入序列
     *      创建一个新的活动节点
     *      将其指向新创建的活动节点
     *      新创建的活动节点的出向指向当前任务节点
     *      删除当前任务
     *      创建新任务
     *
     */

    @Override
    public String execute(CommandContext commandContext) {

        // 获取当前任务实体
        TaskEntity taskEntity = commandContext.getTaskManager().findTaskById(taskId);

        // 获取当前任务的流程定义实例实体
        ProcessDefinitionEntity processDefinition = taskEntity.getProcessDefinition();

        // 当前任务的活动节点
        ActivityImpl currentActivity = processDefinition.findActivity(taskEntity.getTaskDefinitionKey());



        // 创建一个加签节点

        TransitionImpl addSignActivity = new TransitionImpl(null,null);
        // 复制一个当前任务可以否？
        BeanUtils.copyProperties(addSignActivity,currentActivity);
        String id = "Flow_" + RandomUtil.randomString(7);
        addSignActivity.setId(id);

        addSignActivity.setDestination(currentActivity);

        // 删除原来的任务
        commandContext.getTaskManager().deleteTask(taskEntity,null,false,true);
        taskEntity.getExecution().removeTask(taskEntity);

        // 创建新任务
        TaskEntity newTaskEntity = new TaskEntity(taskEntity.getExecution());
        newTaskEntity.setAssignee("新加签的人");
        commandContext.getTaskManager().insertTask(newTaskEntity);
//        // 还原之前的任务流向
//        currentActivity.getIncomingTransitions().remove(addSignActivity);
//        restoreIncomingTransition(currentActivity, pvmTransitionList);
        return null;
    }

    /**
     * 清空指定活动节点流向
     *
     * @param activityImpl 活动节点
     * @return 节点流向集合
     */
    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {
        // 获取当前节点所有流向，存储到临时变量，然后清空
        List<PvmTransition> pvmTransitionList = activityImpl
                .getOutgoingTransitions();
        // 存储当前节点所有流向临时变量
        List<PvmTransition> oriPvmTransitionList = new ArrayList<>(pvmTransitionList);
        pvmTransitionList.clear();
        return oriPvmTransitionList;
    }

    /**
     * 清空指定活动节点流入
     *
     * @param activityImpl 活动节点
     * @return 节点流向集合
     */
    private List<PvmTransition> clearIncomingTransition(ActivityImpl activityImpl) {
        // 获取当前节点所有流向，存储到临时变量，然后清空
        List<PvmTransition> pvmTransitionList = activityImpl
                .getIncomingTransitions();
        // 存储当前节点所有流向临时变量
        List<PvmTransition> oriPvmTransitionList = new ArrayList<>(pvmTransitionList);
        pvmTransitionList.clear();
        return oriPvmTransitionList;
    }

    /**
     * 还原指定活动节点流向
     *
     * @param activityImpl         活动节点
     * @param oriPvmTransitionList 原有节点流向集合
     */
    private void restoreIncomingTransition(ActivityImpl activityImpl, List<PvmTransition> oriPvmTransitionList) {
        // 清空现有流向
        List<PvmTransition> pvmTransitionList = activityImpl.getIncomingTransitions();
        pvmTransitionList.clear();
        // 还原以前流向
        pvmTransitionList.addAll(oriPvmTransitionList);
    }

}
