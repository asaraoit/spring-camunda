package com.asarao.common.process;

import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/*
 * @ClassName: MyParallelMultiInstanceBehavior
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/10 18:31
 * @Version: 1.0
 **/
public class MyParallelMultiInstanceActivityBehavior extends ParallelMultiInstanceActivityBehavior {

//    public MyParallelMultiInstanceActivityBehavior(ActivityImpl activity, AbstractBpmnActivityBehavior originalActivityBehavior) {
////        super(activity, originalActivityBehavior);
//    }
//    //将这个方法executeOriginalBehavior()变成公共的myexecuteOriginalBehavior()
//    public void myexecuteOriginalBehavior(ActivityExecution execution, int loopCounter) throws Exception {
//        if (usesCollection() && collectionElementVariable != null) {
//            Collection collection = null;
//            if (collectionExpression != null) {
//                collection = (Collection) collectionExpression.getValue(execution);
//            } else if (collectionVariable != null) {
//                collection = (Collection) execution.getVariable(collectionVariable);
//            }
//
//            Object value = null;
//            int index = 0;
//            Iterator it = collection.iterator();
//            while (index <= loopCounter) {
//                value = it.next();
//                index++;
//            }
//            setLoopVariable(execution, collectionElementVariable, value);
//        }
//        // If loopcounter == 1, then historic activity instance already created, no need to
//        // pass through executeActivity again since it will create a new historic activity
//        if (loopCounter == 0) {
//            callCustomActivityStartListeners(execution);
//            innerActivityBehavior.execute(execution);
//        } else {
//            execution.executeActivity(activity);
//        }
//    }
}
