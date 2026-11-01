package com.lifeos.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 收支分类实体
 */
@TableName("finance_category")
public class FinanceCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID（NULL 表示系统公共预设分类）
     */
    private Long userId;

    private String categoryName;

    /**
     * 分类类型：INCOME / EXPENSE
     */
    private String categoryType;

    private String icon;

    private Integer sortNo;

    /**
     * 状态：1 启用，0 停用
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public FinanceCategory() {
    }

    public FinanceCategory(Long userId, String categoryName, String categoryType, String icon, Integer sortNo, Integer status) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.icon = icon;
        this.sortNo = sortNo;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

