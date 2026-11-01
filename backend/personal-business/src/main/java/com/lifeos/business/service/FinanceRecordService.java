package com.lifeos.business.service;

import com.lifeos.business.dto.CreateFinanceRecordRequest;
import com.lifeos.business.dto.FinanceRecordQueryRequest;
import com.lifeos.business.dto.UpdateFinanceRecordRequest;
import com.lifeos.business.vo.FinanceCategoryStatVO;
import com.lifeos.business.vo.FinanceRecordVO;
import com.lifeos.business.vo.FinanceSummaryVO;
import com.lifeos.business.vo.FinanceTrendVO;
import com.lifeos.common.api.PageResult;

import java.util.List;

/**
 * 收支明细与财务统计业务服务接口
 */
public interface FinanceRecordService {

    /**
     * 新增收支明细
     */
    FinanceRecordVO createRecord(CreateFinanceRecordRequest request);

    /**
     * 编辑收支明细
     */
    FinanceRecordVO updateRecord(Long id, UpdateFinanceRecordRequest request);

    /**
     * 删除收支明细（软删除）
     */
    void deleteRecord(Long id);

    /**
     * 获取单笔收支详情
     */
    FinanceRecordVO getRecordById(Long id);

    /**
     * 分页多条件查询收支明细
     */
    PageResult<FinanceRecordVO> queryRecords(FinanceRecordQueryRequest request);

    /**
     * 查询月度收支汇总（收入合计、支出合计、结余、分类占比）
     */
    FinanceSummaryVO getMonthlySummary(Integer year, Integer month);

    /**
     * 查询多月收支趋势（收入、支出、结余曲线）
     */
    FinanceTrendVO getTrends(Integer year, Integer months);

    /**
     * 按分类获取月度收支统计明细
     */
    List<FinanceCategoryStatVO> getCategoryStats(Integer year, Integer month, String recordType);
}

