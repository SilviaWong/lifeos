package com.lifeos.statistics.service;

import com.lifeos.common.api.PageResult;
import com.lifeos.statistics.dto.AdminDashboardQueryRequest;
import com.lifeos.statistics.dto.ModuleUsageLogQueryRequest;
import com.lifeos.statistics.vo.ModuleUsageLogVO;
import com.lifeos.statistics.vo.ModuleUsageRankVO;

import java.util.List;

/**
 * 运营管理服务（模块使用排行与操作审计日志查询）
 */
public interface AdminOperationService {

    /**
     * 分页查询模块操作使用审计日志
     *
     * @param request 查询与分页参数
     * @return 分页结果
     */
    PageResult<ModuleUsageLogVO> queryUsageLogs(ModuleUsageLogQueryRequest request);

    /**
     * 获取各业务模块使用量及用户数排行
     *
     * @param request 筛选时间范围
     * @return 模块使用排行列表
     */
    List<ModuleUsageRankVO> getModuleUsageRanking(AdminDashboardQueryRequest request);
}
