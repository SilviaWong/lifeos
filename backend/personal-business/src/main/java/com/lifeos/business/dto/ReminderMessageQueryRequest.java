package com.lifeos.business.dto;

import java.io.Serializable;

/**
 * 站内提醒消息分页查询请求 DTO
 */
public class ReminderMessageQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态过滤：UNREAD（未读，即 PENDING/SENT）, READ（已读）, ALL（全部）
     */
    private String status;

    /**
     * 消息类型：TODO, HABIT, GOAL, SYSTEM, ITEM_DUE
     */
    private String messageType;

    /**
     * 标题或内容模糊搜索关键字
     */
    private String keyword;

    private Integer current = 1;

    private Integer size = 10;

    public ReminderMessageQueryRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getCurrent() {
        if (current == null || current <= 0) {
            return 1;
        }
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        if (size == null || size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}

