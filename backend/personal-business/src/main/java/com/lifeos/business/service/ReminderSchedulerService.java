package com.lifeos.business.service;

/**
 * 提醒规则扫描调度与触发服务
 */
public interface ReminderSchedulerService {

    /**
     * 扫描待触发的提醒规则，并生成对应的站内消息（强幂等保证）
     *
     * @return 实际新生成的站内提醒消息数量
     */
    int scanAndTriggerReminders();
}

