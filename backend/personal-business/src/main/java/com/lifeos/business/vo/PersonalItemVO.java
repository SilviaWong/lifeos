package com.lifeos.business.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 个人物品展示 VO
 */
public class PersonalItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String itemName;
    private String category;
    private BigDecimal quantity;
    private String unit;
    private String storageLocation;
    private LocalDate purchaseDate;
    private String status;
    private String statusDesc;
    private String remark;

    /**
     * 是否借出中：true-借出中, false-在库
     */
    private Boolean isLent = false;

    /**
     * 当前借出记录 ID（若借出中）
     */
    private Long currentLendingId;

    /**
     * 当前借用人姓名（若借出中）
     */
    private String currentBorrower;

    /**
     * 借出时间
     */
    private LocalDateTime currentLentAt;

    /**
     * 预计归还时间
     */
    private LocalDateTime currentExpectedReturnAt;

    /**
     * 是否逾期未归还
     */
    private Boolean isOverdue = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PersonalItemVO() {
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getIsLent() {
        return isLent;
    }

    public void setIsLent(Boolean lent) {
        isLent = lent;
    }

    public Long getCurrentLendingId() {
        return currentLendingId;
    }

    public void setCurrentLendingId(Long currentLendingId) {
        this.currentLendingId = currentLendingId;
    }

    public String getCurrentBorrower() {
        return currentBorrower;
    }

    public void setCurrentBorrower(String currentBorrower) {
        this.currentBorrower = currentBorrower;
    }

    public LocalDateTime getCurrentLentAt() {
        return currentLentAt;
    }

    public void setCurrentLentAt(LocalDateTime currentLentAt) {
        this.currentLentAt = currentLentAt;
    }

    public LocalDateTime getCurrentExpectedReturnAt() {
        return currentExpectedReturnAt;
    }

    public void setCurrentExpectedReturnAt(LocalDateTime currentExpectedReturnAt) {
        this.currentExpectedReturnAt = currentExpectedReturnAt;
    }

    public Boolean getIsOverdue() {
        return isOverdue;
    }

    public void setIsOverdue(Boolean overdue) {
        isOverdue = overdue;
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

