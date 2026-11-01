package com.lifeos.business.service;

import com.lifeos.business.dto.ReminderMessageQueryRequest;
import com.lifeos.business.vo.ReminderExecutionSummaryVO;
import com.lifeos.business.vo.ReminderMessageVO;
import com.lifeos.common.api.PageResult;

/**
 * 站内提醒消息服务接口
 */
public interface ReminderMessageService {

    /**
     * 分页查询站内消息
     */
    PageResult<ReminderMessageVO> queryMessages(ReminderMessageQueryRequest request);

    /**
     * 获取单条消息详情并自动标记为已读
     */
    ReminderMessageVO getMessage(Long id);

    /**
     * 查询未读消息数量
     */
    int countUnread();

    /**
     * 标为已读
     */
    ReminderMessageVO markAsRead(Long id);

    /**
     * 一键全部标为已读
     */
    int markAllAsRead();

    /**
     * 删除单条消息
     */
    void deleteMessage(Long id);

    /**
     * 获取提醒与消息概览统计
     */
    ReminderExecutionSummaryVO getSummary();
}
