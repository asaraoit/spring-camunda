package com.asarao;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * @ClassName: SpringCamundaApplication
 * @Description: TODO
 * @Author: Asarao
 * @Date: 2020/6/5 17:41
 * @Version: 1.0
 **/
@SpringBootApplication
@EnableProcessApplication
public class SpringCamundaApplication {


    public static void main(String[] args) {
        SpringApplication.run(SpringCamundaApplication.class,args);
    }
}
