package com.asarao.common.cmd;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.util.Collection;

/*
 * @ClassName: SetFLowNodeAndGoCmd
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/11 14:34
 * @Version: 1.0
 **/
public class SetFLowNodeAndGoCmd implements Command<Void> {

    private FlowNode flowElement;
    private String executionId;

    public SetFLowNodeAndGoCmd(FlowNode flowElement, String executionId) {
        this.flowElement = flowElement;
        this.executionId = executionId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        //获取目标节点的来源连线
        Collection<SequenceFlow> flows = flowElement.getIncoming();
        if(null != flows && flows.isEmpty()){
            //随便选一条连线来执行，当前执行计划为，从连线流转到目标节点，实现跳转
            ExecutionEntity executionEntity =
                    commandContext.getExecutionManager().findExecutionById(executionId);

        }

        return null;
    }
}
