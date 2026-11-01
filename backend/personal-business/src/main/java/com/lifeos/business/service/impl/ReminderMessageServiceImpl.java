package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.ReminderMessageQueryRequest;
import com.lifeos.business.entity.ReminderMessage;
import com.lifeos.business.entity.ReminderRule;
import com.lifeos.business.mapper.ReminderMessageMapper;
import com.lifeos.business.mapper.ReminderRuleMapper;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.service.ReminderMessageService;
import com.lifeos.business.vo.ReminderExecutionSummaryVO;
import com.lifeos.business.vo.ReminderMessageVO;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 站内提醒消息业务实现
 */
@Service
public class ReminderMessageServiceImpl implements ReminderMessageService {

    private final ReminderMessageMapper reminderMessageMapper;
    private final ReminderRuleMapper reminderRuleMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public ReminderMessageServiceImpl(ReminderMessageMapper reminderMessageMapper,
                                      ReminderRuleMapper reminderRuleMapper,
                                      ModuleUsageLogService moduleUsageLogService) {
        this.reminderMessageMapper = reminderMessageMapper;
        this.reminderRuleMapper = reminderRuleMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    public PageResult<ReminderMessageVO> queryMessages(ReminderMessageQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        if (request == null) {
            request = new ReminderMessageQueryRequest();
        }

        Page<ReminderMessage> pageParam = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<ReminderMessage> qw = new LambdaQueryWrapper<>();
        qw.eq(ReminderMessage::getUserId, userId);
        qw.eq(ReminderMessage::getDeleted, 0);

        if (StringUtils.hasText(request.getStatus())) {
            String s = request.getStatus().trim().toUpperCase();
            if ("UNREAD".equals(s)) {
                qw.in(ReminderMessage::getStatus, "PENDING", "SENT");
            } else if ("READ".equals(s)) {
                qw.eq(ReminderMessage::getStatus, "READ");
            }
        }

        if (StringUtils.hasText(request.getMessageType())) {
            qw.eq(ReminderMessage::getMessageType, request.getMessageType().trim().toUpperCase());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            qw.and(wrapper -> wrapper.like(ReminderMessage::getTitle, kw).or().like(ReminderMessage::getContent, kw));
        }

        qw.orderByDesc(ReminderMessage::getCreatedAt);

        Page<ReminderMessage> pageResult = reminderMessageMapper.selectPage(pageParam, qw);
        List<ReminderMessage> records = pageResult.getRecords();

        if (records == null || records.isEmpty()) {
            return PageResult.empty((int) pageResult.getCurrent(), (int) pageResult.getSize());
        }

        List<ReminderMessageVO> voList = new ArrayList<>(records.size());
        for (ReminderMessage m : records) {
            voList.add(convertToVO(m));
        }

        return PageResult.of(voList, pageResult.getTotal(), (int) pageResult.getCurrent(), (int) pageResult.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderMessageVO getMessage(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        ReminderMessage message = getValidMessage(id, userId);

        // 如果处于未读状态，自动标记为已读
        if (!"READ".equalsIgnoreCase(message.getStatus())) {
            message.setStatus("READ");
            message.setReadAt(LocalDateTime.now());
            reminderMessageMapper.updateById(message);
        }

        return convertToVO(message);
    }

    @Override
    public int countUnread() {
        Long userId = SecurityUtil.getLoginUserId();
        return reminderMessageMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReminderMessageVO markAsRead(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        ReminderMessage message = getValidMessage(id, userId);

        if (!"READ".equalsIgnoreCase(message.getStatus())) {
            message.setStatus("READ");
            message.setReadAt(LocalDateTime.now());
            reminderMessageMapper.updateById(message);
        }

        moduleUsageLogService.logUsage(userId, "REMINDER_MESSAGE", "MARK_READ", id);
        return convertToVO(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsRead() {
        Long userId = SecurityUtil.getLoginUserId();
        int affected = reminderMessageMapper.markAllAsRead(userId, LocalDateTime.now());
        moduleUsageLogService.logUsage(userId, "REMINDER_MESSAGE", "MARK_ALL_READ", null);
        return affected;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        ReminderMessage message = getValidMessage(id, userId);

        reminderMessageMapper.deleteById(message.getId());
        moduleUsageLogService.logUsage(userId, "REMINDER_MESSAGE", "DELETE", id);
    }

    @Override
    public ReminderExecutionSummaryVO getSummary() {
        Long userId = SecurityUtil.getLoginUserId();

        // 规则总数与启用数
        LambdaQueryWrapper<ReminderRule> rqw = new LambdaQueryWrapper<>();
        rqw.eq(ReminderRule::getUserId, userId).eq(ReminderRule::getDeleted, 0);
        int totalRules = Math.toIntExact(reminderRuleMapper.selectCount(rqw));

        rqw.eq(ReminderRule::getStatus, 1);
        int activeRules = Math.toIntExact(reminderRuleMapper.selectCount(rqw));

        // 未读数
        int unread = reminderMessageMapper.countUnreadByUserId(userId);

        // 今日接收数
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<ReminderMessage> mqw = new LambdaQueryWrapper<>();
        mqw.eq(ReminderMessage::getUserId, userId)
           .eq(ReminderMessage::getDeleted, 0)
           .ge(ReminderMessage::getCreatedAt, startOfToday);
        int todayMessages = Math.toIntExact(reminderMessageMapper.selectCount(mqw));

        return new ReminderExecutionSummaryVO(totalRules, activeRules, unread, todayMessages);
    }

    private ReminderMessage getValidMessage(Long id, Long userId) {
        ReminderMessage message = reminderMessageMapper.selectById(id);
        if (message == null || message.getDeleted() != 0 || !message.getUserId().equals(userId)) {
            throw new BusinessException(404, "站内消息不存在或无权限访问");
        }
        return message;
    }

    private ReminderMessageVO convertToVO(ReminderMessage m) {
        ReminderMessageVO vo = new ReminderMessageVO();
        vo.setId(m.getId());
        vo.setUserId(m.getUserId());
        vo.setRuleId(m.getRuleId());
        vo.setTitle(m.getTitle());
        vo.setContent(m.getContent());
        vo.setMessageType(m.getMessageType());
        vo.setMessageTypeDesc(formatMessageTypeDesc(m.getMessageType()));
        vo.setBusinessRefId(m.getBusinessRefId());
        vo.setScheduledAt(m.getScheduledAt());
        vo.setSentAt(m.getSentAt());
        vo.setReadAt(m.getReadAt());
        vo.setStatus(m.getStatus());
        vo.setIsRead("READ".equalsIgnoreCase(m.getStatus()));
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }

    private String formatMessageTypeDesc(String type) {
        if (type == null) return "系统通知";
        return switch (type.toUpperCase()) {
            case "TODO" -> "待办提醒";
            case "HABIT" -> "打卡提醒";
            case "GOAL" -> "目标提醒";
            case "ITEM_DUE" -> "物品归还提醒";
            case "SYSTEM" -> "系统通知";
            default -> type;
        };
    }
}
