package com.lifeos.business.service;

import com.lifeos.business.dto.HealthStandardDTO;
import com.lifeos.business.entity.HealthStandard;
import com.lifeos.business.vo.HealthStandardVO;
import com.lifeos.common.api.PageQuery;
import com.lifeos.common.api.PageResult;

import java.util.List;

/**
 * 健康指标参考标准配置服务
 */
public interface HealthStandardService {

    /**
     * 创建参考标准（管理员）
     */
    HealthStandardVO createStandard(HealthStandardDTO dto, Long adminUserId);

    /**
     * 修改参考标准（管理员）
     */
    HealthStandardVO updateStandard(Long id, HealthStandardDTO dto);

    /**
     * 删除参考标准（管理员）
     */
    void deleteStandard(Long id);

    /**
     * 获取所有当前生效中的健康指标标准列表（用户端与评测通用）
     */
    List<HealthStandardVO> getActiveStandards();

    /**
     * 分页查询参考标准（管理端）
     */
    PageResult<HealthStandardVO> queryStandards(PageQuery query);

    /**
     * 根据指标编码查找当前生效的标准
     */
    HealthStandard findActiveStandardByMetric(String metricCode);
}

