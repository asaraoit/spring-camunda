package com.asarao.task;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/*
 * @ClassName: MyJavaDelegate
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/11 15:37
 * @Version: 1.0
 **/
public class MyJavaDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        execution.setVariable("variablesName","value");
    }
}
