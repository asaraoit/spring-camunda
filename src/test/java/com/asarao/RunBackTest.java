package com.asarao;

import com.asarao.common.process.TaskFlowControlService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/*
 * @ClassName: RunBackTest
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/11 13:59
 * @Version: 1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class RunBackTest {

    @Autowired
    TaskFlowControlService taskFlowControlService;

    @Test
    public void runBack(){
        String taskId = "751605fd-ab8c-11ea-a505-000ec6dd34b8";
        taskFlowControlService.taskRollback(taskId);
    }
}
