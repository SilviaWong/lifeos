package com.lifeos.business.scheduler;

import com.lifeos.business.service.ReminderSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 提醒规则定时扫描任务
 */
@Component
@ConditionalOnProperty(name = "lifeos.reminder.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ReminderScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduledTask.class);

    private final ReminderSchedulerService reminderSchedulerService;

    public ReminderScheduledTask(ReminderSchedulerService reminderSchedulerService) {
        this.reminderSchedulerService = reminderSchedulerService;
    }

    /**
     * 每分钟整点执行一次扫描调度
     */
    @Scheduled(cron = "0 * * * * ?")
    public void executeReminderScan() {
        try {
            reminderSchedulerService.scanAndTriggerReminders();
        } catch (Exception e) {
            log.error("定时扫描触发提醒规则发生异常: {}", e.getMessage(), e);
        }
    }
}

