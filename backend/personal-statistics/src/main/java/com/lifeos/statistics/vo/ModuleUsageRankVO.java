package com.lifeos.statistics.vo;

import java.io.Serializable;

/**
 * 业务模块使用排行与统计 VO
 */
public class ModuleUsageRankVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模块编码（如 TODO, HEALTH, GOAL 等）
     */
    private String moduleCode;

    /**
     * 模块中文显示名称（如 待办清单）
     */
    private String moduleName;

    /**
     * 统计时间区间内该模块操作总次数
     */
    private Long count = 0L;

    /**
     * 统计时间区间内使用该模块的去重独立用户数
     */
    private Long uniqueUsers = 0L;

    /**
     * 占全平台模块总调用量的百分比（如 32.5）
     */
    private Double percentage = 0.0;

    public ModuleUsageRankVO() {
    }

    public ModuleUsageRankVO(String moduleCode, String moduleName, Long count, Long uniqueUsers, Double percentage) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.count = count != null ? count : 0L;
        this.uniqueUsers = uniqueUsers != null ? uniqueUsers : 0L;
        this.percentage = percentage != null ? percentage : 0.0;
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getUniqueUsers() {
        return uniqueUsers;
    }

    public void setUniqueUsers(Long uniqueUsers) {
        this.uniqueUsers = uniqueUsers;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}

