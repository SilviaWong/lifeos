package com.lifeos.statistics.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 模块单项每日使用量时间序列数据 VO
 */
public class ModuleDailyStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 模块中文显示名
     */
    private String moduleName;

    /**
     * 与日期序列一一对应的每日调用次数
     */
    private List<Long> counts = new ArrayList<>();

    public ModuleDailyStatVO() {
    }

    public ModuleDailyStatVO(String moduleCode, String moduleName, List<Long> counts) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.counts = counts != null ? counts : new ArrayList<>();
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public List<Long> getCounts() {
        return counts;
    }

    public void setCounts(List<Long> counts) {
        this.counts = counts;
    }
}

