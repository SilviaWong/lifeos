package com.lifeos.business.dto;

import java.io.Serializable;

/**
 * 物品分页筛选查询请求 DTO
 */
public class ItemQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物品名称/位置/备注关键字搜索
     */
    private String keyword;

    /**
     * 分类筛选
     */
    private String category;

    /**
     * 状态筛选：ACTIVE, ARCHIVED
     */
    private String status;

    /**
     * 借出状态过滤：true-仅看借出中, false-仅看在库, null-全部
     */
    private Boolean isLent;

    private Integer current = 1;
    private Integer size = 10;

    public ItemQueryRequest() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsLent() {
        return isLent;
    }

    public void setIsLent(Boolean isLent) {
        this.isLent = isLent;
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

