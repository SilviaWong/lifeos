package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.HealthStandardDTO;
import com.lifeos.business.entity.HealthStandard;
import com.lifeos.business.enums.HealthMetricType;
import com.lifeos.business.mapper.HealthStandardMapper;
import com.lifeos.business.service.HealthStandardService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.HealthStandardVO;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.api.PageQuery;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 健康指标参考标准配置服务实现
 */
@Service
public class HealthStandardServiceImpl implements HealthStandardService {

    private static final Logger log = LoggerFactory.getLogger(HealthStandardServiceImpl.class);

    private final HealthStandardMapper healthStandardMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public HealthStandardServiceImpl(HealthStandardMapper healthStandardMapper,
                                    ModuleUsageLogService moduleUsageLogService) {
        this.healthStandardMapper = healthStandardMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthStandardVO createStandard(HealthStandardDTO dto, Long adminUserId) {
        if (dto == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "标准配置信息不能为空");
        }
        HealthMetricType metricType = HealthMetricType.fromCode(dto.getMetricCode());
        if (metricType == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "不支持的健康指标类型: " + dto.getMetricCode());
        }

        validateRange(dto.getMinValue(), dto.getMaxValue());

        HealthStandard standard = new HealthStandard();
        standard.setMetricCode(metricType.getCode());
        standard.setMetricName(StringUtils.hasText(dto.getMetricName()) ? dto.getMetricName().trim() : metricType.getName());
        standard.setUnit(StringUtils.hasText(dto.getUnit()) ? dto.getUnit().trim() : metricType.getDefaultUnit());
        standard.setMinValue(dto.getMinValue());
        standard.setMaxValue(dto.getMaxValue());
        standard.setApplicableScope(StringUtils.hasText(dto.getApplicableScope()) ? dto.getApplicableScope().trim().toUpperCase() : "GENERAL");
        standard.setDescription(dto.getDescription());
        standard.setEffectiveFrom(dto.getEffectiveFrom());
        standard.setEffectiveTo(dto.getEffectiveTo());
        standard.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        standard.setCreatedBy(adminUserId);

        healthStandardMapper.insert(standard);
        if (adminUserId != null) {
            moduleUsageLogService.logUsage(adminUserId, "HEALTH", "CREATE_STANDARD", standard.getId());
        }
        log.info("Admin user {} created health standard {} for metric {}", adminUserId, standard.getId(), standard.getMetricCode());

        return convertToVO(standard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthStandardVO updateStandard(Long id, HealthStandardDTO dto) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "标准 ID 不能为空");
        }
        HealthStandard standard = healthStandardMapper.selectById(id);
        if (standard == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "健康指标参考标准不存在");
        }

        if (dto.getMinValue() != null || dto.getMaxValue() != null) {
            BigDecimal min = dto.getMinValue() != null ? dto.getMinValue() : standard.getMinValue();
            BigDecimal max = dto.getMaxValue() != null ? dto.getMaxValue() : standard.getMaxValue();
            validateRange(min, max);
        }

        if (StringUtils.hasText(dto.getMetricName())) {
            standard.setMetricName(dto.getMetricName().trim());
        }
        if (StringUtils.hasText(dto.getUnit())) {
            standard.setUnit(dto.getUnit().trim());
        }
        if (dto.getMinValue() != null) {
            standard.setMinValue(dto.getMinValue());
        }
        if (dto.getMaxValue() != null) {
            standard.setMaxValue(dto.getMaxValue());
        }
        if (StringUtils.hasText(dto.getApplicableScope())) {
            standard.setApplicableScope(dto.getApplicableScope().trim().toUpperCase());
        }
        if (dto.getDescription() != null) {
            standard.setDescription(dto.getDescription());
        }
        if (dto.getEffectiveFrom() != null) {
            standard.setEffectiveFrom(dto.getEffectiveFrom());
        }
        if (dto.getEffectiveTo() != null) {
            standard.setEffectiveTo(dto.getEffectiveTo());
        }
        if (dto.getStatus() != null) {
            standard.setStatus(dto.getStatus());
        }

        healthStandardMapper.updateById(standard);
        return convertToVO(standard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStandard(Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "标准 ID 不能为空");
        }
        HealthStandard standard = healthStandardMapper.selectById(id);
        if (standard == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "健康指标参考标准不存在");
        }
        healthStandardMapper.deleteById(id);
        log.info("Health standard {} deleted", id);
    }

    @Override
    public List<HealthStandardVO> getActiveStandards() {
        List<HealthStandard> list = healthStandardMapper.selectList(
                new LambdaQueryWrapper<HealthStandard>()
                        .eq(HealthStandard::getStatus, 1)
                        .orderByAsc(HealthStandard::getId)
        );
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<HealthStandardVO> queryStandards(PageQuery query) {
        Page<HealthStandard> page = new Page<>(
                query.getPageNum() != null ? query.getPageNum() : 1L,
                query.getPageSize() != null ? query.getPageSize() : 10L
        );
        Page<HealthStandard> selectPage = healthStandardMapper.selectPage(
                page,
                new LambdaQueryWrapper<HealthStandard>().orderByAsc(HealthStandard::getId)
        );
        List<HealthStandardVO> records = selectPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return PageResult.of(records, selectPage.getTotal(), (int) selectPage.getCurrent(), (int) selectPage.getSize());
    }

    @Override
    public HealthStandard findActiveStandardByMetric(String metricCode) {
        if (!StringUtils.hasText(metricCode)) {
            return null;
        }
        return healthStandardMapper.selectOne(
                new LambdaQueryWrapper<HealthStandard>()
                        .eq(HealthStandard::getMetricCode, metricCode.trim().toUpperCase())
                        .eq(HealthStandard::getStatus, 1)
                        .orderByDesc(HealthStandard::getId)
                        .last("LIMIT 1")
        );
    }

    private void validateRange(BigDecimal min, BigDecimal max) {
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "推荐参考最小值不能大于最大值");
        }
    }

    private HealthStandardVO convertToVO(HealthStandard standard) {
        if (standard == null) {
            return null;
        }
        HealthStandardVO vo = new HealthStandardVO();
        vo.setId(standard.getId());
        vo.setMetricCode(standard.getMetricCode());
        vo.setMetricName(standard.getMetricName());
        vo.setUnit(standard.getUnit());
        vo.setMinValue(standard.getMinValue());
        vo.setMaxValue(standard.getMaxValue());
        vo.setApplicableScope(standard.getApplicableScope());
        vo.setDescription(standard.getDescription());
        vo.setEffectiveFrom(standard.getEffectiveFrom());
        vo.setEffectiveTo(standard.getEffectiveTo());
        vo.setStatus(standard.getStatus());
        vo.setUpdatedAt(standard.getUpdatedAt());
        return vo;
    }
}
