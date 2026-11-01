package com.lifeos.business.vo;

import java.math.BigDecimal;

/**
 * 分类收支统计视图对象
 */
public class FinanceCategoryStatVO {

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String recordType;
    private BigDecimal totalAmount;
    private int recordCount;
    private double percentage;

    public FinanceCategoryStatVO() {
    }

    public FinanceCategoryStatVO(Long categoryId, String categoryName, String categoryIcon,
                                 String recordType, BigDecimal totalAmount, int recordCount, double percentage) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.recordType = recordType;
        this.totalAmount = totalAmount;
        this.recordCount = recordCount;
        this.percentage = percentage;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}

