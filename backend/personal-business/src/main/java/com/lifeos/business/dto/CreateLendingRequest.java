package com.lifeos.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物品登记借出请求 DTO
 */
public class CreateLendingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "借用人姓名不能为空")
    @Size(max = 80, message = "借用人姓名长度不能超过80个字符")
    private String borrowerName;

    @NotNull(message = "借出时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lentAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedReturnAt;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    public CreateLendingRequest() {
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public LocalDateTime getLentAt() {
        return lentAt;
    }

    public void setLentAt(LocalDateTime lentAt) {
        this.lentAt = lentAt;
    }

    public LocalDateTime getExpectedReturnAt() {
        return expectedReturnAt;
    }

    public void setExpectedReturnAt(LocalDateTime expectedReturnAt) {
        this.expectedReturnAt = expectedReturnAt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

