package com.lifeos.business.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物品更新请求 DTO
 */
public class UpdateItemRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "物品名称不能为空")
    @Size(max = 120, message = "物品名称长度不能超过120个字符")
    private String itemName;

    @Size(max = 64, message = "物品分类长度不能超过64个字符")
    private String category;

    @DecimalMin(value = "0.01", message = "物品数量必须大于0")
    private BigDecimal quantity;

    @Size(max = 24, message = "单位长度不能超过24个字符")
    private String unit;

    @Size(max = 120, message = "存放位置长度不能超过120个字符")
    private String storageLocation;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    /**
     * ACTIVE-正常, ARCHIVED-归档
     */
    private String status;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    public UpdateItemRequest() {
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

