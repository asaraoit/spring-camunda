package com.asarao.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * @ClassName: TakeListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/7/1 17:13
 * @Version: 1.0
 **/
@Component
@Slf4j
public class TakeListener implements ExecutionListener {

    private FixedValue assigneeList;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        log.info("线监听执行");
        log.info("监听事件：{}",execution.getEventName());
        String expressionText = assigneeList.getExpressionText();
        Map<String,Object> variables = new HashMap<>(0);
        variables.put("assigneeList", Arrays.asList(expressionText.split(",")));
        // 设置变量
        execution.setVariablesLocal(variables);
    }

    public FixedValue getAssigneeList() {
        return assigneeList;
    }

    public void setAssigneeList(FixedValue assigneeList) {
        this.assigneeList = assigneeList;
    }
}
