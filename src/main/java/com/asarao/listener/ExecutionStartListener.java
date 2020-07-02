package com.asarao.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * @ClassName: ExecutionStartListene
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/7/1 17:00
 * @Version: 1.0
 **/
@Component
@Slf4j
public class ExecutionStartListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) throws Exception {
            log.info("流程执行开始");
//        Map<String,Object> variables = new HashMap<>(0);
//        String expressionText = "A,B";
//        variables.put("assigneeList", Arrays.asList(expressionText.split(",")));
//        // 设置变量
//        execution.setVariablesLocal(variables);
    }


}
