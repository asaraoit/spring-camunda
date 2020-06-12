package com.asarao.common.behavior;

import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

import java.io.Serializable;

/*
 * @ClassName: MyActivityBehavior
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/12 11:41
 * @Version: 1.0
 **/
public class MyActivityBehavior extends TaskActivityBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    //添加了一个是否加签信号
    boolean counterSignAdd = false;

    public MyActivityBehavior(ActivityImpl activity, AbstractBpmnActivityBehavior innerActivityBehavior,
                              boolean counterSignAdd) {
        this.counterSignAdd=counterSignAdd;
    }

    @Override
    public void leave(ActivityExecution execution) {
        super.leave(execution);
    }
}
