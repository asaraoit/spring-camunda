package com.asarao.listener;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/*
 * @ClassName: ProcessEndListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/7/1 18:12
 * @Version: 1.0
 **/
@Component
@Slf4j
public class ProcessEndListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        log.info("流程结束监听");
        String processInstanceId = execution.getProcessInstanceId();
    }
}
