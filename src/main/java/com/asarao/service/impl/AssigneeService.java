package com.asarao.service.impl;

import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Arrays;
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

    public List<String> getAssigneeList(){
        return Arrays.asList("A","B");
    }
}
