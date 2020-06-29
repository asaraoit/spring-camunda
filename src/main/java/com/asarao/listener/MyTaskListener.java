package com.asarao.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/*
 * @ClassName: MyTaskListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/28 16:21
 * @Version: 1.0
 **/
@Component
public class MyTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("任务监听执行开始");
        String[] assigneeList = {"C","D"};
        delegateTask.setVariable("assigneeList", Arrays.asList(assigneeList));
        System.out.println("设置变量完成");
    }
}
