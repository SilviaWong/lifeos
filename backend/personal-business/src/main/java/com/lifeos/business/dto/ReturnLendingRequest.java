package com.lifeos.business.dto;

import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物品登记归还请求 DTO
 */
public class ReturnLendingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实际归还时间（可选，默认当前时间）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnedAt;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    public ReturnLendingRequest() {
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

