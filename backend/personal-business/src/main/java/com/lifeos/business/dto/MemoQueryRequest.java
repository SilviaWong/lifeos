package com.lifeos.business.dto;

import java.io.Serializable;

/**
 * 备忘录分页搜索请求 DTO
 */
public class MemoQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标题或正文关键字模糊搜索
     */
    private String keyword;

    /**
     * 单标签筛选
     */
    private String tag;

    /**
     * 状态：ACTIVE, ARCHIVED
     */
    private String status;

    /**
     * 置顶过滤：1-仅置顶, 0-非置顶, null-全部
     */
    private Integer isPinned;

    private Integer current = 1;
    private Integer size = 10;

    public MemoQueryRequest() {
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
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

