package com.lifeos.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模块关键操作使用日志实体
 */
@TableName("module_usage_log")
public class ModuleUsageLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 业务模块编码，如 TODO, HEALTH, GOAL 等
     */
    private String moduleCode;

    /**
     * 操作编码，如 CREATE, UPDATE, COMPLETE, DELETE 等
     */
    private String actionCode;

    /**
     * 关联业务主键 ID（如待办 ID）
     */
    private Long businessRefId;

    /**
     * 发生时间
     */
    private LocalDateTime occurredAt;

    /**
     * 请求链路唯一跟踪 ID
     */
    private String requestId;

    public ModuleUsageLog() {
    }

    public ModuleUsageLog(Long userId, String moduleCode, String actionCode, Long businessRefId, String requestId) {
        this.userId = userId;
        this.moduleCode = moduleCode;
        this.actionCode = actionCode;
        this.businessRefId = businessRefId;
        this.requestId = requestId;
        this.occurredAt = LocalDateTime.now();
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

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public Long getBusinessRefId() {
        return businessRefId;
    }

    public void setBusinessRefId(Long businessRefId) {
        this.businessRefId = businessRefId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}

