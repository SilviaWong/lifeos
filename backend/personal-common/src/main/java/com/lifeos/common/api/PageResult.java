package com.lifeos.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页响应包装对象
 *
 * @param <T> 数据项类型
 */
@Schema(description = "统一分页结果包装")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页数据列表")
    private List<T> records;

    @Schema(description = "总记录数", example = "42")
    private long total;

    @Schema(description = "当前页码", example = "1")
    private int pageNum;

    @Schema(description = "每页大小", example = "10")
    private int pageSize;

    @Schema(description = "总页数", example = "5")
    private int totalPages;

    public PageResult() {
        this.records = Collections.emptyList();
    }

    public PageResult(List<T> records, long total, int pageNum, int pageSize) {
        this.records = records != null ? records : Collections.emptyList();
        this.total = Math.max(0, total);
        this.pageNum = Math.max(1, pageNum);
        this.pageSize = Math.max(1, pageSize);
        this.totalPages = (int) Math.ceil((double) this.total / this.pageSize);
    }

    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(Collections.emptyList(), 0, pageNum, pageSize);
    }

    // Getters and Setters

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records != null ? records : Collections.emptyList();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = Math.max(0, total);
        if (this.pageSize > 0) {
            this.totalPages = (int) Math.ceil((double) this.total / this.pageSize);
        }
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = Math.max(1, pageNum);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, pageSize);
        if (this.pageSize > 0) {
            this.totalPages = (int) Math.ceil((double) this.total / this.pageSize);
        }
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

