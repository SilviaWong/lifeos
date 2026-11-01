package com.lifeos.statistics.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模块操作审计日志行展示 VO
 */
public class ModuleUsageLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志主键 ID
     */
    private Long id;

    /**
     * 操作用户 ID
     */
    private Long userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 业务模块编码
     */
    private String moduleCode;

    /**
     * 业务模块显示名称
     */
    private String moduleName;

    /**
     * 动作编码
     */
    private String actionCode;

    /**
     * 动作显示名称
     */
    private String actionName;

    /**
     * 关联业务主键 ID
     */
    private Long businessRefId;

    /**
     * 发生时间
     */
    private LocalDateTime occurredAt;

    /**
     * 请求链路唯一标识
     */
    private String requestId;

    public ModuleUsageLogVO() {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
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

