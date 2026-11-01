package com.lifeos.business.service;

import com.lifeos.business.dto.CreateHealthRecordRequest;
import com.lifeos.business.dto.HealthRecordQueryRequest;
import com.lifeos.business.dto.HealthTrendQueryRequest;
import com.lifeos.business.dto.UpdateHealthRecordRequest;
import com.lifeos.business.vo.HealthDashboardSummaryVO;
import com.lifeos.business.vo.HealthRecordVO;
import com.lifeos.business.vo.HealthTrendVO;
import com.lifeos.common.api.PageResult;

/**
 * 个人健康指标记录与趋势服务
 */
public interface HealthRecordService {

    /**
     * 录入健康指标记录
     */
    HealthRecordVO createRecord(CreateHealthRecordRequest request);

    /**
     * 修改健康指标记录
     */
    HealthRecordVO updateRecord(Long id, UpdateHealthRecordRequest request);

    /**
     * 删除健康指标记录
     */
    void deleteRecord(Long id);

    /**
     * 根据 ID 获取健康指标详情
     */
    HealthRecordVO getRecordById(Long id);

    /**
     * 分页查询当前用户健康指标记录
     */
    PageResult<HealthRecordVO> queryRecords(HealthRecordQueryRequest request);

    /**
     * 获取指定指标的多维趋势图表数据
     */
    HealthTrendVO getHealthTrend(HealthTrendQueryRequest request);

    /**
     * 获取用户今日健康看板快照汇总
     */
    HealthDashboardSummaryVO getDashboardSummary();
}

