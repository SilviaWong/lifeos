package com.lifeos.business.dto;

import java.io.Serializable;

/**
 * 借还记录列表查询 DTO
 */
public class LendingQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String borrowerName;
    private String status;

    private Integer current = 1;
    private Integer size = 10;

    public LendingQueryRequest() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCurrent() {
        return current != null && current > 0 ? current : 1;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getSize() {
        if (size == null || size <= 0) return 10;
        return Math.min(size, 100);
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}

