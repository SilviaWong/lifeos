package com.lifeos.business.dto;

import com.lifeos.common.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 目标分页与筛选查询参数
 */
@Schema(description = "目标分页与筛选查询参数")
public class GoalQueryRequest extends PageQuery {

    private static final long serialVersionUID = 1L;

    @Schema(description = "按状态筛选：NOT_STARTED, IN_PROGRESS, COMPLETED, ARCHIVED", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "按优先级筛选：1-高, 2-中, 3-低", example = "1")
    private Integer priority;

    @Schema(description = "关键词模糊搜索（标题或描述）", example = "架构")
    private String keyword;

    @Schema(description = "快捷筛选维度：ALL(全部), ACTIVE(进行中与未开始), COMPLETED(已完成), ARCHIVED(已归档)", example = "ACTIVE")
    private String filterType;

    public GoalQueryRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }
}

