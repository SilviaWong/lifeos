package com.lifeos.business.service;

import com.lifeos.business.dto.CreateFinanceCategoryRequest;
import com.lifeos.business.dto.UpdateFinanceCategoryRequest;
import com.lifeos.business.entity.FinanceCategory;
import com.lifeos.business.vo.FinanceCategoryVO;

import java.util.List;

/**
 * 收支分类业务服务接口
 */
public interface FinanceCategoryService {

    /**
     * 获取用户可用的收支分类列表（系统预设 + 用户自定义）
     */
    List<FinanceCategoryVO> getCategoriesForUser(String categoryType);

    /**
     * 创建用户自定义分类
     */
    FinanceCategoryVO createCustomCategory(CreateFinanceCategoryRequest request);

    /**
     * 修改用户自定义分类
     */
    FinanceCategoryVO updateCustomCategory(Long id, UpdateFinanceCategoryRequest request);

    /**
     * 删除用户自定义分类
     */
    void deleteCustomCategory(Long id);

    /**
     * 管理端：查询所有系统预设分类
     */
    List<FinanceCategoryVO> listAllSystemCategories(String categoryType);

    /**
     * 管理端：新增系统预设分类
     */
    FinanceCategoryVO createSystemCategory(CreateFinanceCategoryRequest request);

    /**
     * 管理端：修改系统预设分类
     */
    FinanceCategoryVO updateSystemCategory(Long id, UpdateFinanceCategoryRequest request);

    /**
     * 管理端：删除系统预设分类
     */
    void deleteSystemCategory(Long id);

    /**
     * 内部校验：获取用户可用且符合收支类型的分类实体
     */
    FinanceCategory getValidCategoryForUser(Long categoryId, Long userId, String expectedType);
}

