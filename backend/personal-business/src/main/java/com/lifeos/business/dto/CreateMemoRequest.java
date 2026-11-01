package com.lifeos.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 备忘录创建请求 DTO
 */
public class CreateMemoRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "备忘录标题不能为空")
    @Size(max = 160, message = "标题长度不能超过160个字符")
    private String title;

    private String content;

    @Size(max = 500, message = "标签总长度不能超过500个字符")
    private String tags;

    private Integer isPinned = 0;

    public CreateMemoRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getIsPinned() {
        return isPinned;
    }

    public void setIsPinned(Integer isPinned) {
        this.isPinned = isPinned;
    }
}

