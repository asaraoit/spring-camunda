package com.asarao.listener;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;

/*
 * @ClassName: ExecutionEndListener
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/29 20:10
 * @Version: 1.0
 **/
@Component
public class ExecutionEndListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        System.out.println("Execution End Listener");
        FlowElement element = execution.getBpmnModelElementInstance();
        ExtensionElements extensionElements = element.getExtensionElements();
        CamundaProperties camundaProperties = extensionElements.getElementsQuery().filterByType(CamundaProperties.class).singleResult();
        CamundaProperty property = camundaProperties.getCamundaProperties().iterator().next();
        System.out.println(property.getCamundaName() + "==" + property.getCamundaValue());

        DelegateExecution superExecution = execution.getSuperExecution();
    }
}
