package com.lifeos.statistics.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 各业务模块每日使用趋势响应 VO
 */
public class AdminModuleTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期列表（如 ["2026-10-26", "2026-10-27", ...]）
     */
    private List<String> dates = new ArrayList<>();

    /**
     * 各模块每日序列数据
     */
    private List<ModuleDailyStatVO> series = new ArrayList<>();

    public AdminModuleTrendVO() {
    }

    public AdminModuleTrendVO(List<String> dates, List<ModuleDailyStatVO> series) {
        this.dates = dates != null ? dates : new ArrayList<>();
        this.series = series != null ? series : new ArrayList<>();
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<ModuleDailyStatVO> getSeries() {
        return series;
    }

    public void setSeries(List<ModuleDailyStatVO> series) {
        this.series = series;
    }
}

