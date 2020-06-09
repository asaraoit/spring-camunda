package com.asarao.common.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @ClassName: ProcessService
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/9 13:01
 * @Version: 1.0
 **/
@Service
@Slf4j
@Deprecated
public class ProcessService {

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 流程撤回操作
     *
     * @param taskId 要取回的任务ID
     *               此任务已经执行完成，在历史任务中，现在要跳过，后面的任务节点
     */
    public void withDraw(String taskId) {
        try {
            // 取得当前任务(act_hi_taskinst)
            HistoricTaskInstance currentTask = historyService
                    .createHistoricTaskInstanceQuery()
                    .taskId(taskId)
                    .singleResult();

            // 根据流程实列id查询待办任务中流程信息(act_ru_task)
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(currentTask.getProcessInstanceId()).list();
            if(null != tasks && !tasks.isEmpty()){
                for (Task task:tasks) {
                    // 取回流程节点 (当前任务id, 取回任务id)
                    callBackProcess(task.getId(), currentTask.getId());
                    // 删除历史流程走向记录
                    historyService.deleteHistoricTaskInstance(task.getId());

                }
            }
            // 删除历史流程走向记录
            historyService.deleteHistoricTaskInstance(currentTask.getId());
            log.info("流程取回成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("流程取回失败，错误信息；{}", e.getMessage());
        }
    }

    /**
     * 驳回流程
     *
     * @param taskId     当前任务ID
     * @param activityId 驳回节点ID
     * @param variables  流程存储参数
     * @throws Exception
     */
    public void backProcess(String taskId, String activityId,
                            Map<String, Object> variables) throws Exception {
        if (StringUtils.isEmpty(activityId)) {
            throw new Exception("驳回目标节点ID为空！");
        }

        // 查找所有并行任务节点，同时驳回  
        List<Task> taskList = findTaskListByKey(findProcessInstanceByTaskId(
                taskId).getId(), findTaskById(taskId).getTaskDefinitionKey());
        for (Task task : taskList) {
            commitProcess(task.getId(), variables, activityId);
        }
    }

    /**
     * 取回流程
     *
     * @param taskId     当前任务的id
     * @param activityId 目标节点历史任务ID
     */
    public void callBackProcess(String taskId, String activityId) throws Exception {
        if (StringUtils.isEmpty(activityId)) {
            throw new Exception("目标节点ID为空");
        }
        // 1. 根据当前运行任务的ID，获取到流程实列
        ProcessInstance processInstance = findProcessInstanceByTaskId(taskId);
        // 2. 获取当前运行任务
        Task taskById = findTaskById(taskId);

        // 查找所有并行任务节点，同时取回
        List<Task> taskList = findTaskListByKey(processInstance.getId(),
                taskById.getTaskDefinitionKey());

        for (Task task : taskList) {
            commitProcess(task.getId(), null, activityId);
        }
    }

    /**
     * 终止流程（特权人直接审批通过）
     *
     * @param taskId
     */
    public void endProcess(String taskId) {
        try {
            ActivityImpl endActivity = findActivityImpl(taskId, "end");
            commitProcess(taskId, null, endActivity.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据流入任务集合，查询最近一次的流入任务节点
     *
     * @param processInstance 流程实例
     * @param tempList        流入任务集合
     * @return
     */
    private ActivityImpl filterNewestActivity(ProcessInstance processInstance,
                                              List<ActivityImpl> tempList) {
        while (tempList.size() > 0) {
            ActivityImpl activity_1 = tempList.get(0);
            HistoricActivityInstance activityInstance_1 = findHistoricUserTask(
                    processInstance, activity_1.getId());
            if (activityInstance_1 == null) {
                tempList.remove(activity_1);
                continue;
            }
            if (tempList.size() > 1) {
                ActivityImpl activity_2 = tempList.get(1);
                HistoricActivityInstance activityInstance_2 = findHistoricUserTask(
                        processInstance, activity_2.getId());
                if (activityInstance_2 == null) {
                    tempList.remove(activity_2);
                    continue;
                }

                if (activityInstance_1.getEndTime().before(
                        activityInstance_2.getEndTime())) {
                    tempList.remove(activity_1);
                } else {
                    tempList.remove(activity_2);
                }
            } else {
                break;
            }
        }
        if (tempList.size() > 0) {
            return tempList.get(0);
        }
        return null;
    }

    /**
     * 根据当前任务ID，查询可以驳回的任务节点
     *
     * @param taskId 当前任务ID
     */
    public List<ActivityImpl> findBackAvtivity(String taskId) throws Exception {
        List<ActivityImpl> rtnList = iteratorBackActivity(taskId, findActivityImpl(taskId,
                null), new ArrayList<ActivityImpl>(),
                new ArrayList<ActivityImpl>());
        return reverseList(rtnList);
    }

    /**
     * 转办流程
     * @param taskId 任务ID
     * @param userCode 被转办人
     */
    public void transferAssignee(String taskId,String userCode){
        taskService.setAssignee(taskId,userCode);
    }

    /**
     * 迭代循环流程树结构，查询当前节点可驳回的任务节点
     *
     * @param taskId       当前任务ID
     * @param currActivity 当前活动节点
     * @param rtnList      存储回退节点集合
     * @param tempList     临时存储节点集合（存储一次迭代过程中的同级userTask节点）
     * @return 回退节点集合
     */
    private List<ActivityImpl> iteratorBackActivity(String taskId,
                                                    ActivityImpl currActivity, List<ActivityImpl> rtnList,
                                                    List<ActivityImpl> tempList) throws Exception {
        // 查询流程定义，生成流程树结构
        ProcessInstance processInstance = findProcessInstanceByTaskId(taskId);

        // 当前节点的流入来源
        List<PvmTransition> incomingTransitions = currActivity
                .getIncomingTransitions();
        // 条件分支节点集合，userTask节点遍历完毕，迭代遍历此集合，查询条件分支对应的userTask节点
        List<ActivityImpl> exclusiveGateways = new ArrayList<>();
        // 并行节点集合，userTask节点遍历完毕，迭代遍历此集合，查询并行节点对应的userTask节点
        List<ActivityImpl> parallelGateways = new ArrayList<>();
        // 遍历当前节点所有流入路径
        for (PvmTransition pvmTransition : incomingTransitions) {
            TransitionImpl transitionImpl = (TransitionImpl) pvmTransition;
            ActivityImpl activityImpl = transitionImpl.getSource();
            String type = (String) activityImpl.getProperty("type");
            /**
             * 并行节点配置要求：<br>
             * 必须成对出现，且要求分别配置节点ID为:XXX_start(开始)，XXX_end(结束)
             */
            if ("parallelGateway".equals(type)) {// 并行路线
                String gatewayId = activityImpl.getId();
                String gatewayType = gatewayId.substring(gatewayId
                        .lastIndexOf("_") + 1);
                if ("START".equals(gatewayType.toUpperCase())) {// 并行起点，停止递归
                    return rtnList;
                } else {// 并行终点，临时存储此节点，本次循环结束，迭代集合，查询对应的userTask节点
                    parallelGateways.add(activityImpl);
                }
            } else if ("startEvent".equals(type)) {// 开始节点，停止递归
                return rtnList;
            } else if ("userTask".equals(type)) {// 用户任务
                tempList.add(activityImpl);
            } else if ("exclusiveGateway".equals(type)) {// 分支路线，临时存储此节点，本次循环结束，迭代集合，查询对应的userTask节点
                currActivity = transitionImpl.getSource();
                exclusiveGateways.add(currActivity);
            }
        }

        /**
         * 迭代条件分支集合，查询对应的userTask节点
         */
        for (ActivityImpl activityImpl : exclusiveGateways) {
            iteratorBackActivity(taskId, activityImpl, rtnList, tempList);
        }

        /**
         * 迭代并行集合，查询对应的userTask节点
         */
        for (ActivityImpl activityImpl : parallelGateways) {
            iteratorBackActivity(taskId, activityImpl, rtnList, tempList);
        }

        /**
         * 根据同级userTask集合，过滤最近发生的节点
         */
        currActivity = filterNewestActivity(processInstance, tempList);
        if (currActivity != null) {
            // 查询当前节点的流向是否为并行终点，并获取并行起点ID
            String id = findParallelGatewayId(currActivity);
            if (StringUtils.isEmpty(id)) {// 并行起点ID为空，此节点流向不是并行终点，符合驳回条件，存储此节点
                rtnList.add(currActivity);
            } else {// 根据并行起点ID查询当前节点，然后迭代查询其对应的userTask任务节点
                currActivity = findActivityImpl(taskId, id);
            }

            // 清空本次迭代临时集合
            tempList.clear();
            // 执行下次迭代
            iteratorBackActivity(taskId, currActivity, rtnList, tempList);
        }
        return rtnList;
    }

    /**
     * 根据当前节点，查询输出流向是否为并行终点，如果为并行终点，则拼装对应的并行起点ID
     *
     * @param activityImpl 当前节点
     * @return
     */
    private String findParallelGatewayId(ActivityImpl activityImpl) {
        List<PvmTransition> incomingTransitions = activityImpl
                .getOutgoingTransitions();
        for (PvmTransition pvmTransition : incomingTransitions) {
            TransitionImpl transitionImpl = (TransitionImpl) pvmTransition;
            PvmActivity destination = transitionImpl.getDestination();
//        activityImpl = transitionImpl.getDestination();
            String type = (String) activityImpl.getProperty("type");
            if ("parallelGateway".equals(type)) {// 并行路线
                String gatewayId = activityImpl.getId();
                String gatewayType = gatewayId.substring(gatewayId
                        .lastIndexOf("_") + 1);
                if ("END".equals(gatewayType.toUpperCase())) {
                    return gatewayId.substring(0, gatewayId.lastIndexOf("_"))
                            + "_start";
                }
            }
        }
        return null;
    }

    /**
     * 反向排序list集合，便于驳回节点按顺序显示
     *
     * @param list
     * @return
     */
    private List<ActivityImpl> reverseList(List<ActivityImpl> list) {
        List<ActivityImpl> rtnList = new ArrayList<ActivityImpl>();
        // 由于迭代出现重复数据，排除重复
        for (int i = list.size(); i > 0; i--) {
            if (!rtnList.contains(list.get(i - 1)))
                rtnList.add(list.get(i - 1));
        }
        return rtnList;
    }

    /**
     * 查询指定任务节点的最新记录
     *
     * @param processInstance 流程实例
     * @param activityId
     * @return
     */
    private HistoricActivityInstance findHistoricUserTask(ProcessInstance processInstance, String activityId) {
        HistoricActivityInstance rtnVal = null;
        // 查询当前流程实例审批结束的历史节点
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().activityType("userTask")
                .processInstanceId(processInstance.getId()).activityId(activityId).finished()
                .orderByHistoricActivityInstanceEndTime().desc().list();
        if (historicActivityInstances.size() > 0) {
            rtnVal = historicActivityInstances.get(0);
        }
        return rtnVal;
    }

    /**
     * 根据流程实例ID和任务key值查询所有同级任务集合
     *
     * @param processInstanceId 流程实例ID
     * @param key               任务定义key
     * @return
     */
    private List<Task> findTaskListByKey(String processInstanceId, String key) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(key)
                .list();
    }

    /**
     * 根据任务ID查询流程实例
     *
     * @param taskId 任务ID
     * @return
     */
    public ProcessInstance findProcessInstanceByTaskId(String taskId) throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(findTaskById(taskId).getProcessInstanceId())
                .singleResult();
        if (null == processInstance) {
            throw new Exception("未找到流程实列");
        }
        return processInstance;
    }

    /**
     * 根据任务ID查找任务
     *
     * @param taskId 任务ID
     * @return
     */
    public Task findTaskById(String taskId) throws Exception {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new Exception("未找到任务实列");
        }
        return task;
    }

    /**
     * 提交流程/流程转向
     *
     * @param taskId     任务ID
     * @param variables  流程变量
     * @param activityId 流程转向任务节点的ID，此参数为空，默认为提交操作
     */
    private void commitProcess(String taskId, Map<String, Object> variables, String activityId) throws Exception {
        if (variables == null) {
            variables = new HashMap<>(0);
        }
        // 跳转节点为空，默认为提交操作
        if (StringUtils.isEmpty(activityId)) {
            taskService.complete(taskId, variables);
            // 流程转向操作
        } else {
            turnTransition(taskId, activityId, variables);
        }
    }

    /**
     * 流程转向操作
     *
     * @param taskId     当前任务ID
     * @param activityId 目标节点任务ID
     * @param variables  流程变量
     * @throws Exception
     */
    private void turnTransition(String taskId, String activityId, Map<String, Object> variables) throws Exception {
        // 当前节点
        ActivityImpl currentActivity = findActivityImpl(taskId, null);
        // 清空当前流向
        List<PvmTransition> oriPvmTransitionList = clearTransition(currentActivity);
        // 创建新流向
        TransitionImpl newTransition = currentActivity.createOutgoingTransition();
        // 目标节点
        ActivityImpl pointActivity = findActivityImpl(taskId, activityId);
        // 设值新流向的目标节点
        newTransition.setDestination(pointActivity);
        // 执行转向任务
        taskService.complete(taskId, variables);
        // 删除目标节点新流入
        pointActivity.getIncomingTransitions().remove(newTransition);
        // 还原以前流向
        restoreTransition(currentActivity, oriPvmTransitionList);
    }

    /**
     * 根据任务ID和节点ID获取活动节点
     *
     * @param taskId     任务ID
     * @param activityId 活动节点ID <br>
     *                   如果为null或 ""，则默认查询当前活动节点 <br>
     *                   如果为"end"，则查询结束节点 <br>
     */
    private ActivityImpl findActivityImpl(String taskId, String activityId) throws Exception {
        // 取得流程定义
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);
        // 获取当前活动节点ID
        if (StringUtils.isEmpty(activityId)) {
            activityId = findTaskById(taskId).getTaskDefinitionKey() + ":" + taskId;
        } else {
            HistoricTaskInstance currentTask =
                    historyService.createHistoricTaskInstanceQuery().taskId(activityId).singleResult();
            activityId = currentTask.getTaskDefinitionKey() + ":" + currentTask.getId();
        }
        // 根据流程定义，获取该流程实例的结束节点
        if (activityId.toUpperCase().equals("END")) {
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {
                List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
                if (pvmTransitionList.isEmpty()) {
                    return activityImpl;
                }
            }
        }
        // 根据节点ID，获取对应的活动节点
        ActivityImpl activityImpl =   processDefinition.findActivity(activityId);
        return activityImpl;
    }

    /**
     * 根据任务ID获取流程定义
     *
     * @param taskId 任务ID
     * @return
     */
    public ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(String taskId) throws Exception {
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity)
                repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(findTaskById(taskId).getProcessDefinitionId())
                        .singleResult();
        if (processDefinition == null) {
            throw new Exception("未找到流程定义");
        }
        return processDefinition;
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
     * 还原指定活动节点流向
     *
     * @param activityImpl         活动节点
     * @param oriPvmTransitionList 原有节点流向集合
     */
    private void restoreTransition(ActivityImpl activityImpl, List<PvmTransition> oriPvmTransitionList) {
        // 清空现有流向
        List<PvmTransition> pvmTransitionList = activityImpl.getOutgoingTransitions();
        pvmTransitionList.clear();
        // 还原以前流向
        pvmTransitionList.addAll(oriPvmTransitionList);
    }
}
