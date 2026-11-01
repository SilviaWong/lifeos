package com.lifeos.statistics.service;

import com.lifeos.statistics.dto.AdminDashboardQueryRequest;
import com.lifeos.statistics.vo.AdminDashboardOverviewVO;
import com.lifeos.statistics.vo.AdminModuleTrendVO;

/**
 * 管理端综合数据看板统计服务
 */
public interface AdminDashboardService {

    /**
     * 获取管理端综合数据看板概览指标与图表数据
     *
     * @param request 筛选条件（包含 startDate 和 endDate）
     * @return 综合看板数据
     */
    AdminDashboardOverviewVO getDashboardOverview(AdminDashboardQueryRequest request);

    /**
     * 获取各业务模块每日使用量趋势折线/堆叠数据
     *
     * @param request 筛选条件
     * @return 各模块每日调用序列
     */
    AdminModuleTrendVO getModuleTrends(AdminDashboardQueryRequest request);
}

