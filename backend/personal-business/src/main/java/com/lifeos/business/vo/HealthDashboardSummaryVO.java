package com.lifeos.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户健康看板今日快照视图对象
 */
@Schema(description = "用户健康看板今日快照")
public class HealthDashboardSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "今日累计步数", example = "8500")
    private BigDecimal todaySteps;

    @Schema(description = "步数参考达标情况", example = "NORMAL")
    private String stepsStatus;

    @Schema(description = "今日累计睡眠时长（小时）", example = "7.5")
    private BigDecimal todaySleepHours;

    @Schema(description = "睡眠参考达标情况", example = "NORMAL")
    private String sleepStatus;

    @Schema(description = "今日累计饮水量（ml）", example = "1800")
    private BigDecimal todayWaterMl;

    @Schema(description = "饮水参考达标情况", example = "NORMAL")
    private String waterStatus;

    @Schema(description = "最新记录体重（kg）", example = "68.5")
    private BigDecimal latestWeight;

    @Schema(description = "体重参考情况", example = "NORMAL")
    private String weightStatus;

    @Schema(description = "今日摄入热量（kcal）", example = "1950")
    private BigDecimal todayCalories;

    @Schema(description = "热量参考情况", example = "NORMAL")
    private String caloriesStatus;

    @Schema(description = "健康免责提示", example = "所有指标评估仅供生活方式自律引导，绝不构成任何医学诊断")
    private String disclaimer = "所有指标评估仅供生活方式自律引导，绝不构成任何医学诊断";

    public HealthDashboardSummaryVO() {
        this.todaySteps = BigDecimal.ZERO;
        this.todaySleepHours = BigDecimal.ZERO;
        this.todayWaterMl = BigDecimal.ZERO;
        this.todayCalories = BigDecimal.ZERO;
    }

    public BigDecimal getTodaySteps() {
        return todaySteps;
    }

    public void setTodaySteps(BigDecimal todaySteps) {
        this.todaySteps = todaySteps;
    }

    public String getStepsStatus() {
        return stepsStatus;
    }

    public void setStepsStatus(String stepsStatus) {
        this.stepsStatus = stepsStatus;
    }

    public BigDecimal getTodaySleepHours() {
        return todaySleepHours;
    }

    public void setTodaySleepHours(BigDecimal todaySleepHours) {
        this.todaySleepHours = todaySleepHours;
    }

    public String getSleepStatus() {
        return sleepStatus;
    }

    public void setSleepStatus(String sleepStatus) {
        this.sleepStatus = sleepStatus;
    }

    public BigDecimal getTodayWaterMl() {
        return todayWaterMl;
    }

    public void setTodayWaterMl(BigDecimal todayWaterMl) {
        this.todayWaterMl = todayWaterMl;
    }

    public String getWaterStatus() {
        return waterStatus;
    }

    public void setWaterStatus(String waterStatus) {
        this.waterStatus = waterStatus;
    }

    public BigDecimal getLatestWeight() {
        return latestWeight;
    }

    public void setLatestWeight(BigDecimal latestWeight) {
        this.latestWeight = latestWeight;
    }

    public String getWeightStatus() {
        return weightStatus;
    }

    public void setWeightStatus(String weightStatus) {
        this.weightStatus = weightStatus;
    }

    public BigDecimal getTodayCalories() {
        return todayCalories;
    }

    public void setTodayCalories(BigDecimal todayCalories) {
        this.todayCalories = todayCalories;
    }

    public String getCaloriesStatus() {
        return caloriesStatus;
    }

    public void setCaloriesStatus(String caloriesStatus) {
        this.caloriesStatus = caloriesStatus;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}

