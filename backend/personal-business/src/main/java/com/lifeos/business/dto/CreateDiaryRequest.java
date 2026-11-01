package com.lifeos.business.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 个人日记创建请求 DTO
 */
public class CreateDiaryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "日记日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaryDate;

    @Size(max = 160, message = "日记标题不能超过160个字符")
    private String title;

    @NotBlank(message = "日记正文不能为空")
    private String content;

    @Size(max = 24, message = "心情标识不能超过24个字符")
    private String mood;

    @Size(max = 24, message = "天气标识不能超过24个字符")
    private String weather;

    @Size(max = 500, message = "封面图链接不能超过500个字符")
    private String coverUrl;

    public CreateDiaryRequest() {
    }

    public LocalDate getDiaryDate() {
        return diaryDate;
    }

    public void setDiaryDate(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
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

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}

