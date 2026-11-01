package com.lifeos.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提醒执行幂等日志实体类
 */
@TableName("reminder_execution_log")
public class ReminderExecutionLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long ruleId;

    private Long userId;

    /**
     * 唯一幂等键，数据库唯一约束 uk_reminder_idempotency
     */
    private String idempotencyKey;

    private LocalDateTime executeAt;

    /**
     * 执行结果：SUCCESS, FAILED, SKIPPED
     */
    private String resultStatus;

    /**
     * 异常错误信息
     */
    private String errorMessage;

    private LocalDateTime createdAt;

    public ReminderExecutionLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getExecuteAt() {
        return executeAt;
    }

    public void setExecuteAt(LocalDateTime executeAt) {
        this.executeAt = executeAt;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

