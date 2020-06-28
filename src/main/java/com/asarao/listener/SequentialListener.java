package com.asarao.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * @ClassName: SequentialListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/28 17:27
 * @Version: 1.0
 **/
@Component
public class SequentialListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        System.out.println("执行监听");
        String[] assigneeList = {"B","C"};
        Map<String,Object> variables = new HashMap<>(0);
        variables.put("assigneeList", Arrays.asList(assigneeList));
        execution.setVariablesLocal(variables);
        System.out.println("设置办理集合完成");
    }
}
