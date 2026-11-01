package com.lifeos.business.dto;

import java.io.Serializable;

/**
 * 提醒规则分页查询请求 DTO
 */
public class ReminderRuleQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String triggerType;

    private Integer status;

    private String keyword;

    private Integer current = 1;

    private Integer size = 10;

    public ReminderRuleQueryRequest() {
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

