package com.asarao.task;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.model.bpmn.instance.FlowElement;

/*
 * @ClassName: MyJavaDelegate2
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/11 15:45
 * @Version: 1.0
 **/
public class MyJavaDelegate2 implements ActivityBehavior {
    @Override
    public void execute(ActivityExecution execution) throws Exception {
        FlowElement flowElement = execution.getBpmnModelElementInstance();
    }
}
