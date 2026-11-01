package com.lifeos.business.service.impl;

import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.mapper.ModuleUsageLogMapper;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.common.util.TraceIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 模块关键操作使用记录服务实现
 */
@Service
public class ModuleUsageLogServiceImpl implements ModuleUsageLogService {

    private static final Logger log = LoggerFactory.getLogger(ModuleUsageLogServiceImpl.class);

    private final ModuleUsageLogMapper moduleUsageLogMapper;

    public ModuleUsageLogServiceImpl(ModuleUsageLogMapper moduleUsageLogMapper) {
        this.moduleUsageLogMapper = moduleUsageLogMapper;
    }

    @Override
    public void logUsage(Long userId, String moduleCode, String actionCode, Long businessRefId) {
        if (userId == null || moduleCode == null || actionCode == null) {
            return;
        }
        try {
            String traceId = TraceIdUtil.getTraceId();
            ModuleUsageLog usageLog = new ModuleUsageLog(userId, moduleCode, actionCode, businessRefId, traceId);
            moduleUsageLogMapper.insert(usageLog);
        } catch (Exception e) {
            // 操作审计日志记录失败不阻断核心主业务流
            log.warn("Failed to write module_usage_log for userId={}, module={}, action={}, error={}",
                    userId, moduleCode, actionCode, e.getMessage());
        }
    }
}

