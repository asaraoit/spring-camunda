package com.asarao.task;

import org.camunda.bpm.model.bpmn.impl.instance.UserTaskImpl;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;

/*
 * @ClassName: CustomerTask
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/28 19:08
 * @Version: 1.0
 **/
public class CustomerTask extends UserTaskImpl {

    public CustomerTask(ModelTypeInstanceContext context) {
        super(context);
    }
}
