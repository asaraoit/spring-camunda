package com.asarao.common.process;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SequentialMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

import java.io.Serializable;
import java.util.Collection;

/*
 * @ClassName: MySequentialMultiInstanceBehavior
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/10 18:20
 * @Version: 1.0
 **/
public class MySequentialMultiInstanceActivityBehavior extends SequentialMultiInstanceActivityBehavior
        implements Serializable {

    private static final long serialVersionUID = 1L;

    //添加了一个是否加签信号
    boolean counterSignAdd = false;

    public MySequentialMultiInstanceActivityBehavior(ActivityImpl activity,
                                                     AbstractBpmnActivityBehavior innerActivityBehavior,
                                                     boolean counterSignAdd) {
        this.counterSignAdd=counterSignAdd;
    }



    @SuppressWarnings("rawtypes")
    @Override
    public void leave(ActivityExecution execution) {
        //主要修改这里
        Collection collection = (Collection) collectionExpression.getValue(execution);
//        int loopCounter = getLoopVariable(execution, getCollectionElementIndexVariable()) + 1;
        int nrOfInstances = getLoopVariable(execution, NUMBER_OF_INSTANCES);
        int nrOfCompletedInstances = getLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES) + 1;
        int nrOfActiveInstances = getLoopVariable(execution, NUMBER_OF_ACTIVE_INSTANCES);
        //如果加签则修改流程变量，主要是修改总数和完成数，然后就没了
//        if(counterSignAdd) {
//            loopCounter = nrOfCompletedInstances-1;
//            nrOfInstances = collection.size();
//            nrOfCompletedInstances=nrOfCompletedInstances-1;
//            counterSignAdd=false;
//        }
//        if (loopCounter != nrOfInstances && !completionConditionSatisfied(execution)) {
//            callActivityEndListeners(execution);
//        }
//
//        setLoopVariable(execution, NUMBER_OF_INSTANCES, nrOfInstances);
//        setLoopVariable(execution, getCollectionElementIndexVariable(), loopCounter);
//        setLoopVariable(execution, NUMBER_OF_COMPLETED_INSTANCES, nrOfCompletedInstances);
//        logLoopDetails(execution, "instance completed", loopCounter, nrOfCompletedInstances, nrOfActiveInstances, nrOfInstances);
//
//        if (loopCounter >= nrOfInstances || completionConditionSatisfied(execution)) {
//            super.leave(execution);
//        } else {
//            try {
//                executeOriginalBehavior(execution, loopCounter);
//            } catch (BpmnError error) {
//                // re-throw business fault so that it can be caught by an Error Intermediate Event or Error Event Sub-Process in the process
//                throw error;
//            } catch (Exception e) {
//                throw new ActivitiException("Could not execute inner activity behavior of multi instance behavior", e);
//            }
//        }
    }
}
