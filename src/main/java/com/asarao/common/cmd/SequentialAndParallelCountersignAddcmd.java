package com.asarao.common.cmd;

import org.camunda.bpm.dmn.engine.impl.el.JuelExpression;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SequentialMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/*
 * @ClassName: SequentialAndParallelCountersignAddcmd
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/10 17:40
 * @Version: 1.0
 **/
public class SequentialAndParallelCountersignAddcmd implements Command<String> {

    protected String taskId;

    protected String assignee;

    private RuntimeService runtimeService;

    private TaskService taskService;

    private Boolean isBefore;

    public SequentialAndParallelCountersignAddcmd(String taskId, String assignee, RuntimeService runtimeService,
                                                  TaskService taskService, Boolean isBefore) {
        super();
        this.taskId = taskId;
        this.assignee = assignee;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.isBefore = isBefore;
    }

    @Override
    public String execute(CommandContext commandContext) {
        // 根据任务ID 获取任务
        TaskEntity task = commandContext.getTaskManager().findTaskById(taskId);

        //根据流程定义ID获取流程定义实例
        ProcessDefinitionEntity latestProcessDefinitionById = commandContext
                .getProcessDefinitionManager()
                .findLatestProcessDefinitionById(task.getProcessDefinitionId());

        // 根据任务定义的key获取任务节点
        ActivityImpl activity = latestProcessDefinitionById.findActivity(task.getTaskDefinitionKey());

        // 获取任务节点的所有行为
        ActivityBehavior activityBehavior = activity.getActivityBehavior();

        // 获取执行当前任务的执行实例
        ExecutionEntity execution = commandContext.getExecutionManager().findExecutionById(task.getExecutionId());

        // 表单式集合
        String expressionCollectionName = getCollectionName(activity);
        if (activityBehavior instanceof SequentialMultiInstanceActivityBehavior) {
            //串行前或后加签
            //将现有任务删除( cascade 级联删除，skipCustomerListeners 跳过自定义的监听器)
            commandContext.getTaskManager().deleteTask(task,TaskEntity.DELETE_REASON_DELETED,true,true);
            // 执行实例移除任务
            execution.removeTask(task);
            //给Activity设置自定义behavior
            SequentialMultiInstanceActivityBehavior sequentialMultiInstanceActivityBehavior =
                    (SequentialMultiInstanceActivityBehavior) activityBehavior;
            // 内部活动节点？
            ActivityImpl innerActivity = sequentialMultiInstanceActivityBehavior.getInnerActivity(activity);
//            AbstractBpmnActivityBehavior userTaskActivityBehavior=sequentialMultiInstanceBehavior.getInnerActivityBehavior();
//            MySequentialMultiInstanceBehavior mySequentialMultiInstanceBehavior=new MySequentialMultiInstanceBehavior(activity,userTaskActivityBehavior,true);
//            BeanUtils.copyProperties(sequentialMultiInstanceBehavior, mySequentialMultiInstanceBehavior);
//            userTaskActivityBehavior.setMultiInstanceActivityBehavior(mySequentialMultiInstanceBehavior);
//            activity.setActivityBehavior(mySequentialMultiInstanceBehavior);
//            //修改流程变量
//            List<Object> assigneeList=new ArrayList<>();
//            assigneeList=(List<Object>) runtimeService.getVariable(ee.getId(), expressionCollectionName);
//            int index=assigneeList.indexOf(Integer.parseInt(te.getAssignee()));
//            if(index<0){
//                index=assigneeList.indexOf(te.getAssignee());
//            }
//            if(isBefore) {
//                //插入指定位置之前
//                assigneeList.add(index,assignee);
//            }else {
//                //插入指定位置之后
//                assigneeList.add(index+1,assignee);
//            }
//            runtimeService.setVariable(ee.getId(), expressionCollectionName, assigneeList);
//            //执行实例设置Activity，再转信号执行
//            ee.setActivity(activity);
//            ee.signal(null, null);
            //要将Behavior重写一下，实现串行加签功能
        }else if(activityBehavior instanceof ParallelMultiInstanceActivityBehavior){
//            //并行会签加签
//            //根据父节点创建执行实例
//            ExecutionEntity parent=ee.getParent();
//            ExecutionEntity newExecution=parent.createExecution();
//            newExecution.setActive(true);//设置为激活
//            newExecution.setConcurrent(true);//设置为不可缺少
//            newExecution.setScope(false);
//            //修改流程变量
//            List<Object> assigneeList=new ArrayList<>();
//            assigneeList=(List<Object>) runtimeService.getVariable(ee.getId(), expressionCollectionName);
//            assigneeList.add(assignee);
//            runtimeService.setVariable(parent.getId(), expressionCollectionName, assigneeList);
//            int nrOfInstances=(int) runtimeService.getVariable(parent.getId(), "nrOfInstances");
//            int nrOfActiveInstances=(int) runtimeService.getVariable(parent.getId(), "nrOfActiveInstances");
//            runtimeService.setVariable(parent.getId(), "nrOfInstances", nrOfInstances+1);
//            runtimeService.setVariable(parent.getId(), "nrOfActiveInstances", nrOfActiveInstances+1);
//            newExecution.setVariableLocal("loopCounter", nrOfInstances);
//            newExecution.setVariableLocal("assignee", assignee);
//
//            ParallelMultiInstanceBehavior parallelMultiInstanceBehavior=(ParallelMultiInstanceBehavior)activityBehavior;
//            AbstractBpmnActivityBehavior userTaskActivityBehavior=parallelMultiInstanceBehavior.getInnerActivityBehavior();
//            MyParallelMultiInstanceBehavior myParallelMultiInstanceBehavior=new MyParallelMultiInstanceBehavior(activity,userTaskActivityBehavior);
//            BeanUtils.copyProperties(parallelMultiInstanceBehavior, myParallelMultiInstanceBehavior);
//            userTaskActivityBehavior.setMultiInstanceActivityBehavior(myParallelMultiInstanceBehavior);
//            try {
//                myParallelMultiInstanceBehavior.myexecuteOriginalBehavior(newExecution, nrOfInstances);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }else{
            return "该任务不是会签节点";
        }
        return "加签成功";
    }

    /**
     * 找多实例Activity中设置的集合名字
     *
     * @param activity
     * @return
     */
    public String getCollectionName(ActivityImpl activity) {

        JuelExpression collectionExpression = null;
        if (activity.getActivityBehavior() instanceof SequentialMultiInstanceActivityBehavior) {
            SequentialMultiInstanceActivityBehavior sequentialMultiInstanceBehavior =
                    (SequentialMultiInstanceActivityBehavior) activity.getActivityBehavior();
            collectionExpression = (JuelExpression) sequentialMultiInstanceBehavior.getCollectionExpression();
        } else if (activity.getActivityBehavior() instanceof ParallelMultiInstanceActivityBehavior) {
            ParallelMultiInstanceActivityBehavior parallelMultiInstanceBehavior =
                    (ParallelMultiInstanceActivityBehavior) activity.getActivityBehavior();
            collectionExpression = (JuelExpression) parallelMultiInstanceBehavior.getCollectionExpression();
        }

        String expressionText = collectionExpression.toString();
        expressionText = expressionText.replace("$", "");
        expressionText = expressionText.replace("{", "");
        expressionText = expressionText.replace("}", "");
        return expressionText;
    }
}
