package com.lifeos.statistics.service;

import com.lifeos.statistics.vo.HomeSummaryVO;

/**
 * 用户端首页综合摘要聚合服务
 */
public interface HomeSummaryService {

    /**
     * 获取当前登录用户的首页聚合摘要
     *
     * @return 首页摘要数据
     */
    HomeSummaryVO getHomeSummary();
}

