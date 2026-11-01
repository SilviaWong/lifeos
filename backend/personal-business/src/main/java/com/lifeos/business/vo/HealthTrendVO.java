package com.lifeos.business.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 健康指标趋势聚合视图对象
 */
@Schema(description = "健康指标趋势聚合视图")
public class HealthTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "指标类型编码", example = "STEPS")
    private String metricCode;

    @Schema(description = "指标中文名称", example = "步数")
    private String metricName;

    @Schema(description = "计量单位", example = "步")
    private String unit;

    @Schema(description = "参考推荐最小值", example = "6000")
    private BigDecimal standardMinValue;

    @Schema(description = "参考推荐最大值", example = "12000")
    private BigDecimal standardMaxValue;

    @Schema(description = "聚合方式：SUM(当日累计求和), LATEST(最新值), AVG(平均值)", example = "SUM")
    private String aggregateMethod;

    @Schema(description = "趋势数据点序列")
    private List<HealthTrendPointVO> points;

    @Schema(description = "非医疗免责声明", example = "趋势与标准参考仅供个人生活作息与自律分析，不作为任何医学诊断凭据")
    private String disclaimer = "趋势与标准参考仅供个人生活作息与自律分析，不作为任何医学诊断凭据";

    public HealthTrendVO() {
        this.points = Collections.emptyList();
    }

    public String getMetricCode() {
        return metricCode;
    }

    public void setMetricCode(String metricCode) {
        this.metricCode = metricCode;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getStandardMinValue() {
        return standardMinValue;
    }

    public void setStandardMinValue(BigDecimal standardMinValue) {
        this.standardMinValue = standardMinValue;
    }

    public BigDecimal getStandardMaxValue() {
        return standardMaxValue;
    }

    public void setStandardMaxValue(BigDecimal standardMaxValue) {
        this.standardMaxValue = standardMaxValue;
    }

    public String getAggregateMethod() {
        return aggregateMethod;
    }

    public void setAggregateMethod(String aggregateMethod) {
        this.aggregateMethod = aggregateMethod;
    }

    public List<HealthTrendPointVO> getPoints() {
        return points;
    }

    public void setPoints(List<HealthTrendPointVO> points) {
        this.points = points != null ? points : Collections.emptyList();
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}

