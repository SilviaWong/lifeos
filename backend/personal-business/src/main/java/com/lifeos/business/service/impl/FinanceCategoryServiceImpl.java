package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeos.business.dto.CreateFinanceCategoryRequest;
import com.lifeos.business.dto.UpdateFinanceCategoryRequest;
import com.lifeos.business.entity.FinanceCategory;
import com.lifeos.business.enums.FinanceRecordType;
import com.lifeos.business.mapper.FinanceCategoryMapper;
import com.lifeos.business.service.FinanceCategoryService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.FinanceCategoryVO;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收支分类业务服务实现
 */
@Service
public class FinanceCategoryServiceImpl implements FinanceCategoryService {

    private static final Logger log = LoggerFactory.getLogger(FinanceCategoryServiceImpl.class);

    private final FinanceCategoryMapper financeCategoryMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public FinanceCategoryServiceImpl(FinanceCategoryMapper financeCategoryMapper,
                                      ModuleUsageLogService moduleUsageLogService) {
        this.financeCategoryMapper = financeCategoryMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    public List<FinanceCategoryVO> getCategoriesForUser(String categoryType) {
        Long userId = SecurityUtil.getLoginUserId();

        LambdaQueryWrapper<FinanceCategory> wrapper = new LambdaQueryWrapper<FinanceCategory>()
                .and(w -> w.isNull(FinanceCategory::getUserId).or().eq(FinanceCategory::getUserId, userId))
                .eq(FinanceCategory::getStatus, 1);

        if (StringUtils.hasText(categoryType)) {
            FinanceRecordType type = FinanceRecordType.fromCode(categoryType);
            if (type != null) {
                wrapper.eq(FinanceCategory::getCategoryType, type.getCode());
            }
        }

        wrapper.orderByAsc(FinanceCategory::getSortNo).orderByAsc(FinanceCategory::getId);

        List<FinanceCategory> list = financeCategoryMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceCategoryVO createCustomCategory(CreateFinanceCategoryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        FinanceRecordType type = validateAndGetType(request.getCategoryType());

        FinanceCategory category = new FinanceCategory();
        category.setUserId(userId);
        category.setCategoryName(request.getCategoryName().trim());
        category.setCategoryType(type.getCode());
        category.setIcon(request.getIcon());
        category.setSortNo(request.getSortNo() != null ? request.getSortNo() : 50);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        financeCategoryMapper.insert(category);
        moduleUsageLogService.logUsage(userId, "FINANCE_CATEGORY", "CREATE_CUSTOM", category.getId());

        log.info("User {} created custom finance category {} ({})", userId, category.getCategoryName(), category.getId());
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceCategoryVO updateCustomCategory(Long id, UpdateFinanceCategoryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        FinanceCategory category = financeCategoryMapper.selectById(id);
        if (category == null || category.getUserId() == null || !category.getUserId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "分类不存在或无权修改系统公共分类");
        }

        FinanceRecordType type = validateAndGetType(request.getCategoryType());
        category.setCategoryName(request.getCategoryName().trim());
        category.setCategoryType(type.getCode());
        category.setIcon(request.getIcon());
        if (request.getSortNo() != null) {
            category.setSortNo(request.getSortNo());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }

        financeCategoryMapper.updateById(category);
        moduleUsageLogService.logUsage(userId, "FINANCE_CATEGORY", "UPDATE_CUSTOM", category.getId());

        log.info("User {} updated custom finance category {}", userId, category.getId());
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomCategory(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        FinanceCategory category = financeCategoryMapper.selectById(id);
        if (category == null || category.getUserId() == null || !category.getUserId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "分类不存在或无权删除系统公共分类");
        }

        financeCategoryMapper.deleteById(category.getId());
        moduleUsageLogService.logUsage(userId, "FINANCE_CATEGORY", "DELETE_CUSTOM", category.getId());

        log.info("User {} deleted custom finance category {}", userId, category.getId());
    }

    @Override
    public List<FinanceCategoryVO> listAllSystemCategories(String categoryType) {
        LambdaQueryWrapper<FinanceCategory> wrapper = new LambdaQueryWrapper<FinanceCategory>()
                .isNull(FinanceCategory::getUserId);

        if (StringUtils.hasText(categoryType)) {
            FinanceRecordType type = FinanceRecordType.fromCode(categoryType);
            if (type != null) {
                wrapper.eq(FinanceCategory::getCategoryType, type.getCode());
            }
        }
        wrapper.orderByAsc(FinanceCategory::getSortNo).orderByAsc(FinanceCategory::getId);

        List<FinanceCategory> list = financeCategoryMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceCategoryVO createSystemCategory(CreateFinanceCategoryRequest request) {
        Long adminUserId = SecurityUtil.getLoginUserId();
        FinanceRecordType type = validateAndGetType(request.getCategoryType());

        FinanceCategory category = new FinanceCategory();
        category.setUserId(null); // 系统公共分类
        category.setCategoryName(request.getCategoryName().trim());
        category.setCategoryType(type.getCode());
        category.setIcon(request.getIcon());
        category.setSortNo(request.getSortNo() != null ? request.getSortNo() : 10);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        financeCategoryMapper.insert(category);
        moduleUsageLogService.logUsage(adminUserId, "FINANCE_CATEGORY", "CREATE_SYSTEM", category.getId());

        log.info("Admin {} created system finance category {}", adminUserId, category.getId());
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceCategoryVO updateSystemCategory(Long id, UpdateFinanceCategoryRequest request) {
        Long adminUserId = SecurityUtil.getLoginUserId();
        FinanceCategory category = financeCategoryMapper.selectById(id);
        if (category == null || category.getUserId() != null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "系统分类不存在");
        }

        FinanceRecordType type = validateAndGetType(request.getCategoryType());
        category.setCategoryName(request.getCategoryName().trim());
        category.setCategoryType(type.getCode());
        category.setIcon(request.getIcon());
        if (request.getSortNo() != null) {
            category.setSortNo(request.getSortNo());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }

        financeCategoryMapper.updateById(category);
        moduleUsageLogService.logUsage(adminUserId, "FINANCE_CATEGORY", "UPDATE_SYSTEM", category.getId());

        log.info("Admin {} updated system finance category {}", adminUserId, category.getId());
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSystemCategory(Long id) {
        Long adminUserId = SecurityUtil.getLoginUserId();
        FinanceCategory category = financeCategoryMapper.selectById(id);
        if (category == null || category.getUserId() != null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND, "系统分类不存在");
        }

        financeCategoryMapper.deleteById(category.getId());
        moduleUsageLogService.logUsage(adminUserId, "FINANCE_CATEGORY", "DELETE_SYSTEM", category.getId());

        log.info("Admin {} deleted system finance category {}", adminUserId, category.getId());
    }

    @Override
    public FinanceCategory getValidCategoryForUser(Long categoryId, Long userId, String expectedType) {
        if (categoryId == null) {
            return null;
        }
        FinanceCategory category = financeCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "所选收支分类不存在");
        }

        if (category.getStatus() != null && category.getStatus() == 0) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "所选收支分类已被停用");
        }

        // 校验可见性：用户自定义分类只能自己使用，系统分类大家可用
        if (category.getUserId() != null && !category.getUserId().equals(userId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "无权使用其他用户的自定义分类");
        }

        // 校验收支类型匹配性（核心要求：收入分类不能用于支出，支出分类不能用于收入）
        if (expectedType != null && !category.getCategoryType().equalsIgnoreCase(expectedType.trim())) {
            FinanceRecordType catType = FinanceRecordType.fromCode(category.getCategoryType());
            FinanceRecordType recType = FinanceRecordType.fromCode(expectedType);
            String catDesc = catType != null ? catType.getDesc() : category.getCategoryType();
            String recDesc = recType != null ? recType.getDesc() : expectedType;
            throw new BusinessException(CommonErrorCode.BAD_REQUEST,
                    "分类类型不匹配：分类「" + category.getCategoryName() + "」为" + catDesc + "分类，无法用于" + recDesc + "记录");
        }

        return category;
    }

    private FinanceRecordType validateAndGetType(String categoryType) {
        FinanceRecordType type = FinanceRecordType.fromCode(categoryType);
        if (type == null) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "无效的分类类型(必须为INCOME或EXPENSE)");
        }
        return type;
    }

    private FinanceCategoryVO convertToVO(FinanceCategory entity) {
        FinanceCategoryVO vo = new FinanceCategoryVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setCategoryName(entity.getCategoryName());
        vo.setCategoryType(entity.getCategoryType());

        FinanceRecordType type = FinanceRecordType.fromCode(entity.getCategoryType());
        vo.setCategoryTypeDesc(type != null ? type.getDesc() : entity.getCategoryType());

        vo.setIcon(entity.getIcon());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setSystem(entity.getUserId() == null);
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}

