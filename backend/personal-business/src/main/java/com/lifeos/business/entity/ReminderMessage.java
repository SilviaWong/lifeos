package com.lifeos.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内提醒消息实体类
 */
@TableName("reminder_message")
public class ReminderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 产生的提醒规则ID（若由规则触发）
     */
    private Long ruleId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息正文详情
     */
    private String content;

    /**
     * 消息业务类型：TODO, HABIT, GOAL, SYSTEM, ITEM_DUE
     */
    private String messageType;

    /**
     * 业务关联ID
     */
    private Long businessRefId;

    /**
     * 计划推送时间
     */
    private LocalDateTime scheduledAt;

    /**
     * 实际生成/推送时间
     */
    private LocalDateTime sentAt;

    /**
     * 读取时间
     */
    private LocalDateTime readAt;

    /**
     * 状态：PENDING（待发送）, SENT（已发送/未读）, READ（已读）, FAILED（失败）
     */
    private String status;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    public ReminderMessage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Long getBusinessRefId() {
        return businessRefId;
    }

    public void setBusinessRefId(Long businessRefId) {
        this.businessRefId = businessRefId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}

