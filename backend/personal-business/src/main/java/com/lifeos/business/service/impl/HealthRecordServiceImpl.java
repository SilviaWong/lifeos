package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateHealthRecordRequest;
import com.lifeos.business.dto.HealthRecordQueryRequest;
import com.lifeos.business.dto.HealthTrendQueryRequest;
import com.lifeos.business.dto.UpdateHealthRecordRequest;
import com.lifeos.business.entity.HealthRecord;
import com.lifeos.business.entity.HealthStandard;
import com.lifeos.business.enums.HealthMetricType;
import com.lifeos.business.mapper.HealthRecordMapper;
import com.lifeos.business.service.HealthRecordService;
import com.lifeos.business.service.HealthStandardService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.HealthDashboardSummaryVO;
import com.lifeos.business.vo.HealthRecordVO;
import com.lifeos.business.vo.HealthTrendPointVO;
import com.lifeos.business.vo.HealthTrendVO;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 个人健康指标记录与趋势服务实现
 */
@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    private static final Logger log = LoggerFactory.getLogger(HealthRecordServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HealthRecordMapper healthRecordMapper;
    private final HealthStandardService healthStandardService;
    private final ModuleUsageLogService moduleUsageLogService;

    public HealthRecordServiceImpl(HealthRecordMapper healthRecordMapper,
                                   HealthStandardService healthStandardService,
                                   ModuleUsageLogService moduleUsageLogService) {
        this.healthRecordMapper = healthRecordMapper;
        this.healthStandardService = healthStandardService;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthRecordVO createRecord(CreateHealthRecordRequest request) {
        if (request == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();

        HealthMetricType metricType = HealthMetricType.fromCode(request.getMetricCode());
        if (metricType == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "不支持的健康指标类型: " + request.getMetricCode());
        }

        String unit = resolveAndValidateUnit(metricType, request.getUnit());
        validateMetricValue(metricType, request.getMetricValue());

        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setMetricCode(metricType.getCode());
        record.setMetricValue(request.getMetricValue());
        record.setUnit(unit);
        record.setRecordTime(request.getRecordTime() != null ? request.getRecordTime() : LocalDateTime.now());
        record.setNote(request.getNote());
        record.setSource(StringUtils.hasText(request.getSource()) ? request.getSource().trim().toUpperCase() : "MANUAL");

        healthRecordMapper.insert(record);
        moduleUsageLogService.logUsage(userId, "HEALTH", "CREATE_RECORD", record.getId());

        log.info("User {} created health record {} for metric {}", userId, record.getId(), record.getMetricCode());
        return convertToVO(record, metricType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthRecordVO updateRecord(Long id, UpdateHealthRecordRequest request) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "记录 ID 不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();
        HealthRecord record = findAndVerifyRecord(id, userId);

        HealthMetricType metricType = HealthMetricType.fromCode(record.getMetricCode());
        if (metricType == null) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR, "未知指标类型: " + record.getMetricCode());
        }

        if (request.getMetricValue() != null) {
            validateMetricValue(metricType, request.getMetricValue());
            record.setMetricValue(request.getMetricValue());
        }
        if (StringUtils.hasText(request.getUnit())) {
            String unit = resolveAndValidateUnit(metricType, request.getUnit());
            record.setUnit(unit);
        }
        if (request.getRecordTime() != null) {
            record.setRecordTime(request.getRecordTime());
        }
        if (request.getNote() != null) {
            record.setNote(request.getNote());
        }

        healthRecordMapper.updateById(record);
        moduleUsageLogService.logUsage(userId, "HEALTH", "UPDATE_RECORD", record.getId());

        log.info("User {} updated health record {}", userId, record.getId());
        return convertToVO(record, metricType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "记录 ID 不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();
        HealthRecord record = findAndVerifyRecord(id, userId);

        healthRecordMapper.deleteById(record.getId());
        moduleUsageLogService.logUsage(userId, "HEALTH", "DELETE_RECORD", record.getId());

        log.info("User {} deleted health record {}", userId, record.getId());
    }

    @Override
    public HealthRecordVO getRecordById(Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "记录 ID 不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();
        HealthRecord record = findAndVerifyRecord(id, userId);
        HealthMetricType metricType = HealthMetricType.fromCode(record.getMetricCode());
        return convertToVO(record, metricType);
    }

    @Override
    public PageResult<HealthRecordVO> queryRecords(HealthRecordQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getUserId, userId);

        if (StringUtils.hasText(request.getMetricCode())) {
            HealthMetricType metricType = HealthMetricType.fromCode(request.getMetricCode());
            if (metricType != null) {
                wrapper.eq(HealthRecord::getMetricCode, metricType.getCode());
            } else {
                wrapper.eq(HealthRecord::getMetricCode, request.getMetricCode().trim().toUpperCase());
            }
        }
        if (request.getStartDate() != null) {
            wrapper.ge(HealthRecord::getRecordTime, request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            wrapper.le(HealthRecord::getRecordTime, request.getEndDate().atTime(23, 59, 59));
        }

        wrapper.orderByDesc(HealthRecord::getRecordTime)
                .orderByDesc(HealthRecord::getId);

        Page<HealthRecord> page = new Page<>(
                request.getPageNum() != null ? request.getPageNum() : 1L,
                request.getPageSize() != null ? request.getPageSize() : 10L
        );
        Page<HealthRecord> selectPage = healthRecordMapper.selectPage(page, wrapper);

        List<HealthRecordVO> voList = selectPage.getRecords().stream()
                .map(r -> convertToVO(r, HealthMetricType.fromCode(r.getMetricCode())))
                .collect(Collectors.toList());

        return PageResult.of(voList, selectPage.getTotal(), (int) selectPage.getCurrent(), (int) selectPage.getSize());
    }

    @Override
    public HealthTrendVO getHealthTrend(HealthTrendQueryRequest request) {
        if (request == null || !StringUtils.hasText(request.getMetricCode())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "指标类型编码不能为空");
        }
        Long userId = SecurityUtil.getLoginUserId();

        HealthMetricType metricType = HealthMetricType.fromCode(request.getMetricCode());
        if (metricType == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "不支持的健康指标类型: " + request.getMetricCode());
        }

        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : endDate.minusDays(6);

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "起始日期不能晚于截止日期");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > 90) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "趋势查询跨度不能超过 90 天");
        }

        // 查询该时段内所有记录
        List<HealthRecord> records = healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getUserId, userId)
                        .eq(HealthRecord::getMetricCode, metricType.getCode())
                        .ge(HealthRecord::getRecordTime, startDate.atStartOfDay())
                        .le(HealthRecord::getRecordTime, endDate.atTime(23, 59, 59))
                        .orderByAsc(HealthRecord::getRecordTime)
        );

        // 按日期分组
        Map<LocalDate, List<HealthRecord>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(r -> r.getRecordTime().toLocalDate()));

        // 获取指标参考标准
        HealthStandard standard = healthStandardService.findActiveStandardByMetric(metricType.getCode());

        List<HealthTrendPointVO> points = new ArrayList<>();
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
            List<HealthRecord> dayRecords = recordsByDate.getOrDefault(cur, Collections.emptyList());
            BigDecimal dayValue;
            int count = dayRecords.size();

            if ("SUM".equalsIgnoreCase(metricType.getAggregateMethod())) {
                dayValue = dayRecords.stream()
                        .map(HealthRecord::getMetricValue)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            } else if ("LATEST".equalsIgnoreCase(metricType.getAggregateMethod())) {
                if (dayRecords.isEmpty()) {
                    dayValue = null;
                } else {
                    dayValue = dayRecords.get(dayRecords.size() - 1).getMetricValue();
                }
            } else {
                dayValue = dayRecords.stream()
                        .map(HealthRecord::getMetricValue)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            points.add(new HealthTrendPointVO(cur.format(DATE_FORMATTER), dayValue, count));
            cur = cur.plusDays(1);
        }

        HealthTrendVO trendVO = new HealthTrendVO();
        trendVO.setMetricCode(metricType.getCode());
        trendVO.setMetricName(metricType.getName());
        trendVO.setUnit(metricType.getDefaultUnit());
        trendVO.setAggregateMethod(metricType.getAggregateMethod());
        trendVO.setPoints(points);

        if (standard != null) {
            trendVO.setStandardMinValue(standard.getMinValue());
            trendVO.setStandardMaxValue(standard.getMaxValue());
        }

        return trendVO;
    }

    @Override
    public HealthDashboardSummaryVO getDashboardSummary() {
        Long userId = SecurityUtil.getLoginUserId();
        LocalDate today = LocalDate.now();

        List<HealthRecord> todayRecords = healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getUserId, userId)
                        .ge(HealthRecord::getRecordTime, today.atStartOfDay())
                        .le(HealthRecord::getRecordTime, today.atTime(23, 59, 59))
                        .orderByAsc(HealthRecord::getRecordTime)
        );

        HealthDashboardSummaryVO summary = new HealthDashboardSummaryVO();

        // 步数汇总
        List<HealthRecord> stepRecords = filterRecords(todayRecords, HealthMetricType.STEPS.getCode());
        BigDecimal todaySteps = sumValues(stepRecords);
        summary.setTodaySteps(todaySteps);
        HealthStandard stepStd = healthStandardService.findActiveStandardByMetric(HealthMetricType.STEPS.getCode());
        summary.setStepsStatus(stepRecords.isEmpty() ? "NOT_RECORDED" : evaluateStatus(todaySteps, stepStd));

        // 睡眠时长汇总
        List<HealthRecord> sleepRecords = filterRecords(todayRecords, HealthMetricType.SLEEP_HOURS.getCode());
        BigDecimal todaySleep = sumValues(sleepRecords);
        summary.setTodaySleepHours(todaySleep);
        HealthStandard sleepStd = healthStandardService.findActiveStandardByMetric(HealthMetricType.SLEEP_HOURS.getCode());
        summary.setSleepStatus(sleepRecords.isEmpty() ? "NOT_RECORDED" : evaluateStatus(todaySleep, sleepStd));

        // 饮水汇总
        List<HealthRecord> waterRecords = filterRecords(todayRecords, HealthMetricType.WATER_ML.getCode());
        BigDecimal todayWater = sumValues(waterRecords);
        summary.setTodayWaterMl(todayWater);
        HealthStandard waterStd = healthStandardService.findActiveStandardByMetric(HealthMetricType.WATER_ML.getCode());
        summary.setWaterStatus(waterRecords.isEmpty() ? "NOT_RECORDED" : evaluateStatus(todayWater, waterStd));

        // 饮食摄入卡路里汇总
        List<HealthRecord> calRecords = filterRecords(todayRecords, HealthMetricType.CALORIES.getCode());
        BigDecimal todayCalories = sumValues(calRecords);
        summary.setTodayCalories(todayCalories);
        HealthStandard calStd = healthStandardService.findActiveStandardByMetric(HealthMetricType.CALORIES.getCode());
        summary.setCaloriesStatus(calRecords.isEmpty() ? "NOT_RECORDED" : evaluateStatus(todayCalories, calStd));

        // 体重：优先取今日最新，今日无则取历史最近一条
        List<HealthRecord> todayWeightRecords = filterRecords(todayRecords, HealthMetricType.WEIGHT.getCode());
        HealthStandard weightStd = healthStandardService.findActiveStandardByMetric(HealthMetricType.WEIGHT.getCode());
        if (!todayWeightRecords.isEmpty()) {
            BigDecimal latestWeight = todayWeightRecords.get(todayWeightRecords.size() - 1).getMetricValue();
            summary.setLatestWeight(latestWeight);
            summary.setWeightStatus(evaluateStatus(latestWeight, weightStd));
        } else {
            HealthRecord latestWeightRec = healthRecordMapper.selectOne(
                    new LambdaQueryWrapper<HealthRecord>()
                            .eq(HealthRecord::getUserId, userId)
                            .eq(HealthRecord::getMetricCode, HealthMetricType.WEIGHT.getCode())
                            .orderByDesc(HealthRecord::getRecordTime)
                            .orderByDesc(HealthRecord::getId)
                            .last("LIMIT 1")
            );
            if (latestWeightRec != null) {
                summary.setLatestWeight(latestWeightRec.getMetricValue());
                summary.setWeightStatus(evaluateStatus(latestWeightRec.getMetricValue(), weightStd));
            } else {
                summary.setLatestWeight(null);
                summary.setWeightStatus("NOT_RECORDED");
            }
        }

        return summary;
    }

    private List<HealthRecord> filterRecords(List<HealthRecord> records, String code) {
        return records.stream()
                .filter(r -> code.equalsIgnoreCase(r.getMetricCode()))
                .collect(Collectors.toList());
    }

    private BigDecimal sumValues(List<HealthRecord> records) {
        return records.stream()
                .map(HealthRecord::getMetricValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private HealthRecord findAndVerifyRecord(Long id, Long userId) {
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "健康记录不存在或无权访问");
        }
        return record;
    }

    private String resolveAndValidateUnit(HealthMetricType metricType, String requestedUnit) {
        if (!StringUtils.hasText(requestedUnit)) {
            return metricType.getDefaultUnit();
        }
        String unit = requestedUnit.trim();
        if (!metricType.isUnitSupported(unit)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST,
                    String.format("指标单位 [%s] 不合法，支持的单位为: %s", unit, metricType.getSupportedUnits()));
        }
        return unit;
    }

    private void validateMetricValue(HealthMetricType metricType, BigDecimal value) {
        if (value == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "测量数值不能为空");
        }
        if (!metricType.isValueInRange(value)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST,
                    String.format("指标数值超出合理范围 [%s ~ %s %s]",
                            metricType.getMinAllowed(), metricType.getMaxAllowed(), metricType.getDefaultUnit()));
        }
    }

    private String evaluateStatus(BigDecimal value, HealthStandard standard) {
        if (value == null || standard == null) {
            return "UNKNOWN";
        }
        if (standard.getMinValue() != null && value.compareTo(standard.getMinValue()) < 0) {
            return "BELOW";
        }
        if (standard.getMaxValue() != null && value.compareTo(standard.getMaxValue()) > 0) {
            return "ABOVE";
        }
        return "NORMAL";
    }

    private HealthRecordVO convertToVO(HealthRecord record, HealthMetricType metricType) {
        if (record == null) {
            return null;
        }
        HealthRecordVO vo = new HealthRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setMetricCode(record.getMetricCode());
        vo.setMetricName(metricType != null ? metricType.getName() : record.getMetricCode());
        vo.setMetricValue(record.getMetricValue());
        vo.setUnit(record.getUnit());
        vo.setRecordTime(record.getRecordTime());
        vo.setNote(record.getNote());
        vo.setSource(record.getSource());
        vo.setCreatedAt(record.getCreatedAt());

        // 评估当前参考标准
        HealthStandard standard = healthStandardService.findActiveStandardByMetric(record.getMetricCode());
        String status = evaluateStatus(record.getMetricValue(), standard);
        vo.setStandardStatus(status);
        switch (status) {
            case "NORMAL":
                vo.setStandardHint("生活参考推荐区间内（非医疗诊断）");
                break;
            case "BELOW":
                vo.setStandardHint("低于生活参考推荐区间（非医疗诊断，仅供生活自律参考）");
                break;
            case "ABOVE":
                vo.setStandardHint("高于生活参考推荐区间（非医疗诊断，仅供生活自律参考）");
                break;
            default:
                vo.setStandardHint("暂无参考标准（仅供生活习惯记录）");
                break;
        }

        return vo;
    }
}
