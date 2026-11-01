package com.lifeos.business.vo;

import java.time.LocalDateTime;

/**
 * 收支分类视图对象
 */
public class FinanceCategoryVO {

    private Long id;
    private Long userId;
    private String categoryName;
    private String categoryType;
    private String categoryTypeDesc;
    private String icon;
    private Integer sortNo;
    private Integer status;
    private boolean isSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FinanceCategoryVO() {
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

    public String getCategoryTypeDesc() {
        return categoryTypeDesc;
    }

    public void setCategoryTypeDesc(String categoryTypeDesc) {
        this.categoryTypeDesc = categoryTypeDesc;
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

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
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

