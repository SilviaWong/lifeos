package com.lifeos.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

/**
 * 统一分页查询请求基类
 */
@Schema(description = "统一分页查询参数")
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "页码必须大于等于 1")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    @Schema(description = "每页条数，范围 1~100", example = "10")
    @Min(value = 1, message = "每页条数必须大于等于 1")
    @Max(value = MAX_PAGE_SIZE, message = "每页条数不能超过 100")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public PageQuery() {
    }

    public PageQuery(Integer pageNum, Integer pageSize) {
        this.setPageNum(pageNum);
        this.setPageSize(pageSize);
    }

    public Integer getPageNum() {
        if (pageNum == null || pageNum < 1) {
            return DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        if (pageNum != null && pageNum >= 1) {
            this.pageNum = pageNum;
        } else {
            this.pageNum = DEFAULT_PAGE_NUM;
        }
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        } else if (pageSize > MAX_PAGE_SIZE) {
            this.pageSize = MAX_PAGE_SIZE;
        } else {
            this.pageSize = pageSize;
        }
    }

    public long getOffset() {
        return (long) (getPageNum() - 1) * getPageSize();
    }
}

