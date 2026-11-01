package com.lifeos.business.service;

/**
 * 模块关键操作审计日志服务
 */
public interface ModuleUsageLogService {

    /**
     * 记录模块关键业务操作
     *
     * @param userId        用户 ID
     * @param moduleCode    模块编码（如 TODO）
     * @param actionCode    操作编码（如 CREATE, UPDATE, COMPLETE, REOPEN, DELETE）
     * @param businessRefId 关联业务实体 ID
     */
    void logUsage(Long userId, String moduleCode, String actionCode, Long businessRefId);
}

