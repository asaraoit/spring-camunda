package com.asarao.service;

public interface ProcessService {

    /**
     * 委派
     */
    void delegate(String taskId,String userId);

    /**
     * 转办
     */
    void turn(String taskId,String assignee);

    /**
     * 抢占
     */
    void preemption(String taskId,String userId);

    /**
     * 会签
     */
    void countersign();

    /**
     * 委托代办
     */
    void concierge(String taskId,String userId);

    /**
     * 催办
     */
    void reminder();

    /**
     * 自由流
     */
    void freeFlow();

    /**
     * 回退
     */
    void fallback();

    /**
     * 取回
     */
    void retrieve();

    /**
     * 指派
     */
    void assign(String taskId,String userId);

    /**
     * 前加签
     */
    void beforeAddSign();

    /**
     * 后加签
     */
    void afterAddSign();

    /**
     * 改派
     */
    void reassign();

    /**
     * 驳回
     */
    void reject();

    /**
     * 终止
     */
    void terminate();

    /**
     * 挂起
     */
    void hang();

    /**
     * 激活
     */
    void active();
}
