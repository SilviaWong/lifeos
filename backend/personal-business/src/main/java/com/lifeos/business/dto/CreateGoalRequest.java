package com.lifeos.business.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 创建目标请求参数
 */
@Schema(description = "创建目标请求参数")
public class CreateGoalRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "目标标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "通过系统架构设计师认证")
    @NotBlank(message = "目标标题不能为空")
    @Size(max = 120, message = "目标标题长度不得超过 120 个字符")
    private String title;

    @Schema(description = "目标详细描述", example = "包含论文写作与案例分析备考")
    @Size(max = 2000, message = "目标描述长度不得超过 2000 个字符")
    private String description;

    @Schema(description = "开始日期 (yyyy-MM-dd)", example = "2026-11-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "截止日期 (yyyy-MM-dd)", example = "2026-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Schema(description = "优先级：1-高, 2-中, 3-低", example = "1")
    @Min(value = 1, message = "优先级最低为 1")
    @Max(value = 3, message = "优先级最高为 3")
    private Integer priority = 2;

    public CreateGoalRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getPriority() {
        return priority != null ? priority : 2;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}

