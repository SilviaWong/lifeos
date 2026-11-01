package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateFinanceRecordRequest;
import com.lifeos.business.dto.FinanceRecordQueryRequest;
import com.lifeos.business.dto.UpdateFinanceRecordRequest;
import com.lifeos.business.entity.FinanceCategory;
import com.lifeos.business.entity.FinanceRecord;
import com.lifeos.business.enums.FinanceRecordType;
import com.lifeos.business.mapper.FinanceCategoryMapper;
import com.lifeos.business.mapper.FinanceRecordMapper;
import com.lifeos.business.service.FinanceCategoryService;
import com.lifeos.business.service.FinanceRecordService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.*;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收支明细与财务统计业务服务实现
 */
@Service
public class FinanceRecordServiceImpl implements FinanceRecordService {

    private static final Logger log = LoggerFactory.getLogger(FinanceRecordServiceImpl.class);

    private final FinanceRecordMapper financeRecordMapper;
    private final FinanceCategoryMapper financeCategoryMapper;
    private final FinanceCategoryService financeCategoryService;
    private final ModuleUsageLogService moduleUsageLogService;

    public FinanceRecordServiceImpl(FinanceRecordMapper financeRecordMapper,
                                    FinanceCategoryMapper financeCategoryMapper,
                                    FinanceCategoryService financeCategoryService,
                                    ModuleUsageLogService moduleUsageLogService) {
        this.financeRecordMapper = financeRecordMapper;
        this.financeCategoryMapper = financeCategoryMapper;
        this.financeCategoryService = financeCategoryService;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceRecordVO createRecord(CreateFinanceRecordRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        FinanceRecordType type = validateAndGetType(request.getRecordType());
        BigDecimal amount = validateAndNormalizeAmount(request.getAmount());

        if (request.getRecordDate() == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "收支日期不能为空");
        }

        // 校验分类有效性与类型匹配度
        FinanceCategory category = financeCategoryService.getValidCategoryForUser(
                request.getCategoryId(), userId, type.getCode()
        );

        String currencyCode = StringUtils.hasText(request.getCurrencyCode())
                ? request.getCurrencyCode().trim().toUpperCase() : "CNY";

        FinanceRecord record = new FinanceRecord();
        record.setUserId(userId);
        record.setCategoryId(category != null ? category.getId() : null);
        record.setRecordType(type.getCode());
        record.setAmount(amount);
        record.setCurrencyCode(currencyCode);
        record.setRecordDate(request.getRecordDate());
        record.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : null);
        record.setRemark(request.getRemark());

        financeRecordMapper.insert(record);
        moduleUsageLogService.logUsage(userId, "FINANCE", "CREATE", record.getId());

        log.info("User {} created finance record id={}, type={}, amount={}",
                userId, record.getId(), type.getCode(), amount);

        return convertSingle(record, category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceRecordVO updateRecord(Long id, UpdateFinanceRecordRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        FinanceRecord record = getOwnedRecord(id, userId);

        FinanceRecordType type = validateAndGetType(request.getRecordType());
        BigDecimal amount = validateAndNormalizeAmount(request.getAmount());

        if (request.getRecordDate() == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "收支日期不能为空");
        }

        FinanceCategory category = financeCategoryService.getValidCategoryForUser(
                request.getCategoryId(), userId, type.getCode()
        );

        String currencyCode = StringUtils.hasText(request.getCurrencyCode())
                ? request.getCurrencyCode().trim().toUpperCase() : "CNY";

        record.setCategoryId(category != null ? category.getId() : null);
        record.setRecordType(type.getCode());
        record.setAmount(amount);
        record.setCurrencyCode(currencyCode);
        record.setRecordDate(request.getRecordDate());
        record.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : null);
        record.setRemark(request.getRemark());

        financeRecordMapper.updateById(record);
        moduleUsageLogService.logUsage(userId, "FINANCE", "UPDATE", record.getId());

        log.info("User {} updated finance record id={}", userId, record.getId());
        return convertSingle(record, category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        FinanceRecord record = getOwnedRecord(id, userId);

        financeRecordMapper.deleteById(record.getId());
        moduleUsageLogService.logUsage(userId, "FINANCE", "DELETE", record.getId());

        log.info("User {} deleted finance record id={}", userId, record.getId());
    }

    @Override
    public FinanceRecordVO getRecordById(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        FinanceRecord record = getOwnedRecord(id, userId);

        FinanceCategory category = null;
        if (record.getCategoryId() != null) {
            category = financeCategoryMapper.selectById(record.getCategoryId());
        }
        return convertSingle(record, category);
    }

    @Override
    public PageResult<FinanceRecordVO> queryRecords(FinanceRecordQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        int current = (request.getPageNo() != null && request.getPageNo() > 0) ? request.getPageNo() : 1;
        int size = (request.getPageSize() != null && request.getPageSize() > 0 && request.getPageSize() <= 100)
                ? request.getPageSize() : 20;

        LambdaQueryWrapper<FinanceRecord> wrapper = new LambdaQueryWrapper<FinanceRecord>()
                .eq(FinanceRecord::getUserId, userId);

        if (StringUtils.hasText(request.getRecordType())) {
            FinanceRecordType type = FinanceRecordType.fromCode(request.getRecordType());
            if (type != null) {
                wrapper.eq(FinanceRecord::getRecordType, type.getCode());
            }
        }
        if (request.getCategoryId() != null) {
            wrapper.eq(FinanceRecord::getCategoryId, request.getCategoryId());
        }

        // 按年月筛选 (例如 2026-11)
        if (StringUtils.hasText(request.getYearMonth())) {
            try {
                YearMonth ym = YearMonth.parse(request.getYearMonth().trim());
                wrapper.ge(FinanceRecord::getRecordDate, ym.atDay(1))
                        .le(FinanceRecord::getRecordDate, ym.atEndOfMonth());
            } catch (Exception e) {
                log.warn("Invalid yearMonth param: {}", request.getYearMonth());
            }
        } else {
            if (request.getStartDate() != null) {
                wrapper.ge(FinanceRecord::getRecordDate, request.getStartDate());
            }
            if (request.getEndDate() != null) {
                wrapper.le(FinanceRecord::getRecordDate, request.getEndDate());
            }
        }

        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            wrapper.and(w -> w.like(FinanceRecord::getTitle, kw).or().like(FinanceRecord::getRemark, kw));
        }

        wrapper.orderByDesc(FinanceRecord::getRecordDate).orderByDesc(FinanceRecord::getId);

        Page<FinanceRecord> page = new Page<>(current, size);
        Page<FinanceRecord> resultPage = financeRecordMapper.selectPage(page, wrapper);

        if (resultPage.getRecords().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), resultPage.getTotal(), current, size);
        }

        // 批量查询分类信息，防止 N+1
        Set<Long> categoryIds = resultPage.getRecords().stream()
                .map(FinanceRecord::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, FinanceCategory> categoryMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            List<FinanceCategory> categories = financeCategoryMapper.selectBatchIds(categoryIds);
            categoryMap = categories.stream().collect(Collectors.toMap(FinanceCategory::getId, c -> c));
        }

        final Map<Long, FinanceCategory> finalCategoryMap = categoryMap;
        List<FinanceRecordVO> voList = resultPage.getRecords().stream()
                .map(r -> convertSingle(r, r.getCategoryId() != null ? finalCategoryMap.get(r.getCategoryId()) : null))
                .collect(Collectors.toList());

        return new PageResult<>(voList, resultPage.getTotal(), current, size);
    }

    @Override
    public FinanceSummaryVO getMonthlySummary(Integer year, Integer month) {
        Long userId = SecurityUtil.getLoginUserId();

        LocalDate now = LocalDate.now();
        int qYear = (year != null && year >= 2000 && year <= 2100) ? year : now.getYear();
        int qMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(qYear, qMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<FinanceRecord> records = financeRecordMapper.selectList(
                new LambdaQueryWrapper<FinanceRecord>()
                        .eq(FinanceRecord::getUserId, userId)
                        .ge(FinanceRecord::getRecordDate, start)
                        .le(FinanceRecord::getRecordDate, end)
        );

        BigDecimal totalIncome = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalExpense = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (FinanceRecord record : records) {
            BigDecimal amt = record.getAmount() != null ? record.getAmount() : BigDecimal.ZERO;
            if (FinanceRecordType.INCOME.getCode().equalsIgnoreCase(record.getRecordType())) {
                totalIncome = totalIncome.add(amt);
            } else if (FinanceRecordType.EXPENSE.getCode().equalsIgnoreCase(record.getRecordType())) {
                totalExpense = totalExpense.add(amt);
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);

        // 分类聚合
        List<FinanceCategoryStatVO> expenseStats = aggregateCategoryStats(records, FinanceRecordType.EXPENSE.getCode(), totalExpense);
        List<FinanceCategoryStatVO> incomeStats = aggregateCategoryStats(records, FinanceRecordType.INCOME.getCode(), totalIncome);

        FinanceSummaryVO vo = new FinanceSummaryVO();
        vo.setYear(qYear);
        vo.setMonth(qMonth);
        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setBalance(balance);
        vo.setRecordCount(records.size());
        vo.setExpenseCategoryStats(expenseStats);
        vo.setIncomeCategoryStats(incomeStats);

        return vo;
    }

    @Override
    public FinanceTrendVO getTrends(Integer year, Integer months) {
        Long userId = SecurityUtil.getLoginUserId();

        int spanMonths = (months != null && months >= 1 && months <= 24) ? months : 6;
        YearMonth currentYm = YearMonth.now();
        if (year != null && year >= 2000 && year <= 2100) {
            currentYm = YearMonth.of(year, 12);
        }

        YearMonth startYm = currentYm.minusMonths(spanMonths - 1);
        LocalDate startDate = startYm.atDay(1);
        LocalDate endDate = currentYm.atEndOfMonth();

        List<FinanceRecord> records = financeRecordMapper.selectList(
                new LambdaQueryWrapper<FinanceRecord>()
                        .eq(FinanceRecord::getUserId, userId)
                        .ge(FinanceRecord::getRecordDate, startDate)
                        .le(FinanceRecord::getRecordDate, endDate)
        );

        // 按 yyyy-MM 分组
        Map<String, List<FinanceRecord>> monthMap = records.stream().collect(
                Collectors.groupingBy(r -> r.getRecordDate().format(DateTimeFormatter.ofPattern("yyyy-MM")))
        );

        List<FinanceTrendPointVO> points = new ArrayList<>();
        BigDecimal totalPeriodIncome = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPeriodExpense = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        YearMonth cursor = startYm;
        while (!cursor.isAfter(currentYm)) {
            String monthKey = cursor.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            List<FinanceRecord> monthRecords = monthMap.getOrDefault(monthKey, Collections.emptyList());

            BigDecimal mIncome = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal mExpense = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            for (FinanceRecord r : monthRecords) {
                BigDecimal amt = r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO;
                if (FinanceRecordType.INCOME.getCode().equalsIgnoreCase(r.getRecordType())) {
                    mIncome = mIncome.add(amt);
                } else if (FinanceRecordType.EXPENSE.getCode().equalsIgnoreCase(r.getRecordType())) {
                    mExpense = mExpense.add(amt);
                }
            }

            BigDecimal mBalance = mIncome.subtract(mExpense);
            points.add(new FinanceTrendPointVO(monthKey, mIncome, mExpense, mBalance));

            totalPeriodIncome = totalPeriodIncome.add(mIncome);
            totalPeriodExpense = totalPeriodExpense.add(mExpense);

            cursor = cursor.plusMonths(1);
        }

        BigDecimal totalPeriodBalance = totalPeriodIncome.subtract(totalPeriodExpense);
        return new FinanceTrendVO(points, totalPeriodIncome, totalPeriodExpense, totalPeriodBalance);
    }

    @Override
    public List<FinanceCategoryStatVO> getCategoryStats(Integer year, Integer month, String recordType) {
        Long userId = SecurityUtil.getLoginUserId();

        LocalDate now = LocalDate.now();
        int qYear = (year != null && year >= 2000 && year <= 2100) ? year : now.getYear();
        int qMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();

        YearMonth ym = YearMonth.of(qYear, qMonth);
        LambdaQueryWrapper<FinanceRecord> wrapper = new LambdaQueryWrapper<FinanceRecord>()
                .eq(FinanceRecord::getUserId, userId)
                .ge(FinanceRecord::getRecordDate, ym.atDay(1))
                .le(FinanceRecord::getRecordDate, ym.atEndOfMonth());

        if (StringUtils.hasText(recordType)) {
            FinanceRecordType type = FinanceRecordType.fromCode(recordType);
            if (type != null) {
                wrapper.eq(FinanceRecord::getRecordType, type.getCode());
            }
        }

        List<FinanceRecord> records = financeRecordMapper.selectList(wrapper);
        String targetType = StringUtils.hasText(recordType) ? recordType.trim().toUpperCase() : FinanceRecordType.EXPENSE.getCode();

        BigDecimal totalAmount = records.stream()
                .filter(r -> targetType.equalsIgnoreCase(r.getRecordType()))
                .map(FinanceRecord::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return aggregateCategoryStats(records, targetType, totalAmount);
    }

    // ================= 私有方法与数据换算 =================

    private List<FinanceCategoryStatVO> aggregateCategoryStats(List<FinanceRecord> records, String targetType, BigDecimal totalAmount) {
        List<FinanceRecord> filtered = records.stream()
                .filter(r -> targetType.equalsIgnoreCase(r.getRecordType()))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> categoryIds = filtered.stream()
                .map(FinanceRecord::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, FinanceCategory> catMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            List<FinanceCategory> categories = financeCategoryMapper.selectBatchIds(categoryIds);
            catMap = categories.stream().collect(Collectors.toMap(FinanceCategory::getId, c -> c));
        }

        // 按 category_id 分组求和
        Map<Long, List<FinanceRecord>> groupMap = filtered.stream()
                .collect(Collectors.groupingBy(r -> r.getCategoryId() != null ? r.getCategoryId() : 0L));

        List<FinanceCategoryStatVO> stats = new ArrayList<>();
        for (Map.Entry<Long, List<FinanceRecord>> entry : groupMap.entrySet()) {
            Long catId = entry.getKey();
            List<FinanceRecord> groupRecords = entry.getValue();

            BigDecimal sum = groupRecords.stream()
                    .map(FinanceRecord::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            double percentage = 0.0;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = sum.divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            String catName = "未分类";
            String catIcon = "other";
            if (catId > 0 && catMap.containsKey(catId)) {
                FinanceCategory c = catMap.get(catId);
                catName = c.getCategoryName();
                catIcon = c.getIcon();
            }

            stats.add(new FinanceCategoryStatVO(
                    catId > 0 ? catId : null,
                    catName,
                    catIcon,
                    targetType,
                    sum,
                    groupRecords.size(),
                    percentage
            ));
        }

        // 按金额从高到低排序
        stats.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        return stats;
    }

    private FinanceRecord getOwnedRecord(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "记录ID不能为空");
        }
        FinanceRecord record = financeRecordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "收支记录不存在或无权访问");
        }
        return record;
    }

    private FinanceRecordType validateAndGetType(String recordType) {
        FinanceRecordType type = FinanceRecordType.fromCode(recordType);
        if (type == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "无效的收支类型(必须为INCOME或EXPENSE)");
        }
        return type;
    }

    private BigDecimal validateAndNormalizeAmount(BigDecimal rawAmount) {
        if (rawAmount == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "金额不能为空");
        }
        BigDecimal normalized = rawAmount.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "金额必须大于0");
        }
        return normalized;
    }

    private FinanceRecordVO convertSingle(FinanceRecord record, FinanceCategory category) {
        FinanceRecordVO vo = new FinanceRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setCategoryId(record.getCategoryId());

        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
            vo.setCategoryIcon(category.getIcon());
        }

        vo.setRecordType(record.getRecordType());
        FinanceRecordType type = FinanceRecordType.fromCode(record.getRecordType());
        vo.setRecordTypeDesc(type != null ? type.getDesc() : record.getRecordType());

        vo.setAmount(record.getAmount());
        vo.setCurrencyCode(record.getCurrencyCode());
        vo.setRecordDate(record.getRecordDate());
        vo.setTitle(record.getTitle());
        vo.setRemark(record.getRemark());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setUpdatedAt(record.getUpdatedAt());
        return vo;
    }
}

