package com.asarao.service.impl;

import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * @ClassName: AssigneeService
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/28 16:57
 * @Version: 1.0
 **/
@Service
public class AssigneeService implements Serializable {

    private static final long serialVersionUID = 1L;

    public List<String> getAssigneeList(int... assignee){
        System.out.println(assignee.length);
        List<String> assigneeList = new ArrayList<>();
        for (int i : assignee) {
            assigneeList.add(Integer.toString(i));
        }
        return assigneeList;
    }

    public String getAssignee(int assignee){
        System.out.println(assignee);
        return Integer.toString(assignee);
    }
}
