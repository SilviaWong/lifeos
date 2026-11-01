package com.lifeos.statistics.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理端综合数据看板聚合响应 VO
 */
public class AdminDashboardOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 筛选时间范围信息
     */
    private DateRangeFilterVO filter;

    /**
     * 概览核心指标卡片（总数与区间数）
     */
    private DashboardCardMetricsVO overviewCards;

    /**
     * 用户新增与活跃每日趋势
     */
    private List<UserTrendPointVO> userTrend = new ArrayList<>();

    /**
     * 业务模块使用量排行
     */
    private List<ModuleUsageRankVO> moduleUsageRank = new ArrayList<>();

    /**
     * 健康数据分类记录数分布
     */
    private List<HealthTypeStatVO> healthDistribution = new ArrayList<>();

    /**
     * 财务收支情况统计
     */
    private FinanceStatSummaryVO financeSummary;

    public AdminDashboardOverviewVO() {
    }

    public DateRangeFilterVO getFilter() {
        return filter;
    }

    public void setFilter(DateRangeFilterVO filter) {
        this.filter = filter;
    }

    public DashboardCardMetricsVO getOverviewCards() {
        return overviewCards;
    }

    public void setOverviewCards(DashboardCardMetricsVO overviewCards) {
        this.overviewCards = overviewCards;
    }

    public List<UserTrendPointVO> getUserTrend() {
        return userTrend;
    }

    public void setUserTrend(List<UserTrendPointVO> userTrend) {
        this.userTrend = userTrend;
    }

    public List<ModuleUsageRankVO> getModuleUsageRank() {
        return moduleUsageRank;
    }

    public void setModuleUsageRank(List<ModuleUsageRankVO> moduleUsageRank) {
        this.moduleUsageRank = moduleUsageRank;
    }

    public List<HealthTypeStatVO> getHealthDistribution() {
        return healthDistribution;
    }

    public void setHealthDistribution(List<HealthTypeStatVO> healthDistribution) {
        this.healthDistribution = healthDistribution;
    }

    public FinanceStatSummaryVO getFinanceSummary() {
        return financeSummary;
    }

    public void setFinanceSummary(FinanceStatSummaryVO financeSummary) {
        this.financeSummary = financeSummary;
    }
}

