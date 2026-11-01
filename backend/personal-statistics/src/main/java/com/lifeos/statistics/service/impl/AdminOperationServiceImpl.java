package com.lifeos.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.mapper.ModuleUsageLogMapper;
import com.lifeos.common.api.PageResult;
import com.lifeos.statistics.dto.AdminDashboardQueryRequest;
import com.lifeos.statistics.dto.ModuleUsageLogQueryRequest;
import com.lifeos.statistics.service.AdminDashboardService;
import com.lifeos.statistics.service.AdminOperationService;
import com.lifeos.statistics.vo.AdminDashboardOverviewVO;
import com.lifeos.statistics.vo.ModuleUsageLogVO;
import com.lifeos.statistics.vo.ModuleUsageRankVO;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 运营管理服务实现（模块使用排行与审计使用日志查询）
 */
@Service
public class AdminOperationServiceImpl implements AdminOperationService {

    private final ModuleUsageLogMapper moduleUsageLogMapper;
    private final SysUserMapper sysUserMapper;
    private final AdminDashboardService adminDashboardService;

    public AdminOperationServiceImpl(ModuleUsageLogMapper moduleUsageLogMapper,
                                     SysUserMapper sysUserMapper,
                                     AdminDashboardService adminDashboardService) {
        this.moduleUsageLogMapper = moduleUsageLogMapper;
        this.sysUserMapper = sysUserMapper;
        this.adminDashboardService = adminDashboardService;
    }

    @Override
    public PageResult<ModuleUsageLogVO> queryUsageLogs(ModuleUsageLogQueryRequest request) {
        if (request == null) {
            request = new ModuleUsageLogQueryRequest();
        }

        int current = request.getCurrent();
        int size = request.getSize();

        Page<ModuleUsageLog> pageParam = new Page<>(current, size);
        QueryWrapper<ModuleUsageLog> qw = new QueryWrapper<>();

        if (StringUtils.hasText(request.getModuleCode())) {
            qw.eq("module_code", request.getModuleCode().trim().toUpperCase());
        }
        if (StringUtils.hasText(request.getActionCode())) {
            qw.eq("action_code", request.getActionCode().trim().toUpperCase());
        }
        if (request.getUserId() != null) {
            qw.eq("user_id", request.getUserId());
        }
        if (StringUtils.hasText(request.getRequestId())) {
            qw.like("request_id", request.getRequestId().trim());
        }
        if (request.getStartDate() != null) {
            qw.ge("occurred_at", request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            qw.le("occurred_at", request.getEndDate().atTime(LocalTime.MAX));
        }

        qw.orderByDesc("occurred_at");

        Page<ModuleUsageLog> resultPage = moduleUsageLogMapper.selectPage(pageParam, qw);
        List<ModuleUsageLog> records = resultPage.getRecords();

        if (records == null || records.isEmpty()) {
            return PageResult.empty((int) resultPage.getCurrent(), (int) resultPage.getSize());
        }

        // 批量查询用户账号和昵称
        Set<Long> userIds = records.stream()
                .map(ModuleUsageLog::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        Map<Long, SysUser> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            List<SysUser> userList = sysUserMapper.selectBatchIds(userIds);
            if (userList != null) {
                userMap = userList.stream().collect(Collectors.toMap(SysUser::getId, u -> u, (u1, u2) -> u1));
            }
        }

        List<ModuleUsageLogVO> voList = new ArrayList<>(records.size());
        for (ModuleUsageLog log : records) {
            ModuleUsageLogVO vo = new ModuleUsageLogVO();
            vo.setId(log.getId());
            vo.setUserId(log.getUserId());
            SysUser user = userMap.get(log.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            } else {
                vo.setUsername("用户#" + log.getUserId());
                vo.setNickname("-");
            }
            vo.setModuleCode(log.getModuleCode());
            vo.setModuleName(getModuleName(log.getModuleCode()));
            vo.setActionCode(log.getActionCode());
            vo.setActionName(getActionName(log.getActionCode()));
            vo.setBusinessRefId(log.getBusinessRefId());
            vo.setOccurredAt(log.getOccurredAt());
            vo.setRequestId(log.getRequestId());
            voList.add(vo);
        }

        return PageResult.of(voList, resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize());
    }

    @Override
    public List<ModuleUsageRankVO> getModuleUsageRanking(AdminDashboardQueryRequest request) {
        AdminDashboardOverviewVO overview = adminDashboardService.getDashboardOverview(request);
        return overview != null && overview.getModuleUsageRank() != null
                ? overview.getModuleUsageRank()
                : Collections.emptyList();
    }

    private String getModuleName(String code) {
        if (code == null) return "未知模块";
        return switch (code.toUpperCase()) {
            case "TODO" -> "待办清单";
            case "GOAL" -> "目标与计划";
            case "HEALTH" -> "健康记录";
            case "POMODORO" -> "番茄专注";
            case "HABIT" -> "习惯打卡";
            case "FINANCE" -> "收支记账";
            default -> code;
        };
    }

    private String getActionName(String code) {
        if (code == null) return "操作";
        return switch (code.toUpperCase()) {
            case "CREATE" -> "新增/创建";
            case "UPDATE" -> "编辑/修改";
            case "DELETE" -> "删除";
            case "COMPLETE" -> "完成";
            case "REOPEN" -> "重新打开";
            case "CHECKIN" -> "日常打卡";
            case "CANCEL" -> "取消打卡";
            case "START" -> "开始会话";
            case "FINISH" -> "正常结束";
            case "ABORT" -> "放弃/中止";
            default -> code;
        };
    }
}
