package com.lifeos.business.service;

import com.lifeos.business.dto.CreateReminderRuleRequest;
import com.lifeos.business.dto.ReminderRuleQueryRequest;
import com.lifeos.business.dto.UpdateReminderRuleRequest;
import com.lifeos.business.vo.ReminderRuleVO;
import com.lifeos.common.api.PageResult;

/**
 * 提醒规则服务接口
 */
public interface ReminderRuleService {

    /**
     * 新增提醒规则
     */
    ReminderRuleVO createRule(CreateReminderRuleRequest request);

    /**
     * 更新提醒规则
     */
    ReminderRuleVO updateRule(Long id, UpdateReminderRuleRequest request);

    /**
     * 删除提醒规则
     */
    void deleteRule(Long id);

    /**
     * 启用 / 停用切换
     */
    ReminderRuleVO toggleRuleStatus(Long id);

    /**
     * 查询提醒规则详情
     */
    ReminderRuleVO getRule(Long id);

    /**
     * 分页查询提醒规则
     */
    PageResult<ReminderRuleVO> queryRules(ReminderRuleQueryRequest request);
}
