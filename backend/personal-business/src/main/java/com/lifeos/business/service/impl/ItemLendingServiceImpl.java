package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateLendingRequest;
import com.lifeos.business.dto.LendingQueryRequest;
import com.lifeos.business.dto.ReturnLendingRequest;
import com.lifeos.business.entity.ItemLending;
import com.lifeos.business.entity.PersonalItem;
import com.lifeos.business.mapper.ItemLendingMapper;
import com.lifeos.business.mapper.PersonalItemMapper;
import com.lifeos.business.service.ItemLendingService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.ItemLendingVO;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物品借还记录服务实现
 */
@Service
public class ItemLendingServiceImpl implements ItemLendingService {

    private final ItemLendingMapper itemLendingMapper;
    private final PersonalItemMapper personalItemMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public ItemLendingServiceImpl(ItemLendingMapper itemLendingMapper,
                                  PersonalItemMapper personalItemMapper,
                                  ModuleUsageLogService moduleUsageLogService) {
        this.itemLendingMapper = itemLendingMapper;
        this.personalItemMapper = personalItemMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemLendingVO lendItem(Long itemId, CreateLendingRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        PersonalItem item = personalItemMapper.selectById(itemId);
        if (item == null || item.getDeleted() != 0 || !item.getUserId().equals(userId)) {
            throw new BusinessException(404, "物品不存在或无权借出");
        }

        if ("ARCHIVED".equalsIgnoreCase(item.getStatus())) {
            throw new BusinessException(400, "已归档物品无法登记借出，请先恢复为正常在用");
        }

        // 核心规则：同一物品同时最多一笔未归还记录
        ItemLending activeLending = itemLendingMapper.selectOne(
                new LambdaQueryWrapper<ItemLending>()
                        .eq(ItemLending::getItemId, itemId)
                        .in(ItemLending::getStatus, Arrays.asList("LENT", "OVERDUE"))
                        .last("LIMIT 1")
        );
        if (activeLending != null) {
            throw new BusinessException(400, "该物品当前已被「" + activeLending.getBorrowerName() + "」借出中，尚未归还，不能重复借出");
        }

        LocalDateTime lentAt = request.getLentAt() != null ? request.getLentAt() : LocalDateTime.now();
        if (request.getExpectedReturnAt() != null && request.getExpectedReturnAt().isBefore(lentAt)) {
            throw new BusinessException(400, "预计归还时间不能早于借出时间");
        }

        ItemLending lending = new ItemLending();
        lending.setUserId(userId);
        lending.setItemId(itemId);
        lending.setBorrowerName(request.getBorrowerName().trim());
        lending.setLentAt(lentAt);
        lending.setExpectedReturnAt(request.getExpectedReturnAt());
        lending.setStatus("LENT");
        lending.setRemark(request.getRemark());
        lending.setCreatedAt(LocalDateTime.now());
        lending.setUpdatedAt(LocalDateTime.now());

        itemLendingMapper.insert(lending);
        moduleUsageLogService.logUsage(userId, "LENDING", "LEND", lending.getId());

        return convertToVO(lending, item.getItemName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemLendingVO returnItem(Long lendingId, ReturnLendingRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        ItemLending lending = itemLendingMapper.selectById(lendingId);
        if (lending == null || !lending.getUserId().equals(userId)) {
            throw new BusinessException(404, "借还记录不存在或无权操作");
        }

        if ("RETURNED".equalsIgnoreCase(lending.getStatus())) {
            throw new BusinessException(400, "该借出记录已登记归还，请勿重复操作");
        }

        LocalDateTime returnTime = (request != null && request.getReturnedAt() != null)
                ? request.getReturnedAt()
                : LocalDateTime.now();

        if (returnTime.isBefore(lending.getLentAt())) {
            throw new BusinessException(400, "归还时间不能早于借出时间");
        }

        lending.setReturnedAt(returnTime);
        lending.setStatus("RETURNED");
        if (request != null && StringUtils.hasText(request.getRemark())) {
            lending.setRemark(request.getRemark());
        }
        lending.setUpdatedAt(LocalDateTime.now());

        itemLendingMapper.updateById(lending);
        moduleUsageLogService.logUsage(userId, "LENDING", "RETURN", lending.getId());

        PersonalItem item = personalItemMapper.selectById(lending.getItemId());
        String itemName = item != null ? item.getItemName() : "物品#" + lending.getItemId();
        return convertToVO(lending, itemName);
    }

    @Override
    public List<ItemLendingVO> getLendingsByItem(Long itemId) {
        Long userId = SecurityUtil.getLoginUserId();
        PersonalItem item = personalItemMapper.selectById(itemId);
        if (item == null || item.getDeleted() != 0 || !item.getUserId().equals(userId)) {
            throw new BusinessException(404, "物品不存在或无权查看");
        }

        List<ItemLending> list = itemLendingMapper.selectList(
                new LambdaQueryWrapper<ItemLending>()
                        .eq(ItemLending::getUserId, userId)
                        .eq(ItemLending::getItemId, itemId)
                        .orderByDesc(ItemLending::getLentAt)
        );

        List<ItemLendingVO> voList = new ArrayList<>();
        for (ItemLending lending : list) {
            voList.add(convertToVO(lending, item.getItemName()));
        }
        return voList;
    }

    @Override
    public PageResult<ItemLendingVO> queryLendings(LendingQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        if (request == null) {
            request = new LendingQueryRequest();
        }

        Page<ItemLending> pageParam = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<ItemLending> qw = new LambdaQueryWrapper<>();
        qw.eq(ItemLending::getUserId, userId);

        if (request.getItemId() != null) {
            qw.eq(ItemLending::getItemId, request.getItemId());
        }
        if (StringUtils.hasText(request.getBorrowerName())) {
            qw.like(ItemLending::getBorrowerName, request.getBorrowerName().trim());
        }
        if (StringUtils.hasText(request.getStatus())) {
            qw.eq(ItemLending::getStatus, request.getStatus().trim().toUpperCase());
        }

        qw.orderByDesc(ItemLending::getLentAt);

        Page<ItemLending> pageResult = itemLendingMapper.selectPage(pageParam, qw);
        List<ItemLending> records = pageResult.getRecords();

        if (records == null || records.isEmpty()) {
            return PageResult.empty((int) pageResult.getCurrent(), (int) pageResult.getSize());
        }

        Set<Long> itemIds = records.stream().map(ItemLending::getItemId).collect(Collectors.toSet());
        Map<Long, String> itemNameMap = Collections.emptyMap();
        if (!itemIds.isEmpty()) {
            List<PersonalItem> items = personalItemMapper.selectBatchIds(itemIds);
            if (items != null) {
                itemNameMap = items.stream().collect(Collectors.toMap(PersonalItem::getId, PersonalItem::getItemName, (a, b) -> a));
            }
        }

        List<ItemLendingVO> voList = new ArrayList<>();
        for (ItemLending lending : records) {
            String itemName = itemNameMap.getOrDefault(lending.getItemId(), "物品#" + lending.getItemId());
            voList.add(convertToVO(lending, itemName));
        }

        return PageResult.of(voList, pageResult.getTotal(), (int) pageResult.getCurrent(), (int) pageResult.getSize());
    }

    private ItemLendingVO convertToVO(ItemLending lending, String itemName) {
        ItemLendingVO vo = new ItemLendingVO();
        vo.setId(lending.getId());
        vo.setUserId(lending.getUserId());
        vo.setItemId(lending.getItemId());
        vo.setItemName(itemName);
        vo.setBorrowerName(lending.getBorrowerName());
        vo.setLentAt(lending.getLentAt());
        vo.setExpectedReturnAt(lending.getExpectedReturnAt());
        vo.setReturnedAt(lending.getReturnedAt());
        vo.setStatus(lending.getStatus());

        boolean isOverdue = !"RETURNED".equalsIgnoreCase(lending.getStatus())
                && lending.getExpectedReturnAt() != null
                && lending.getExpectedReturnAt().isBefore(LocalDateTime.now());
        vo.setIsOverdue(isOverdue);

        if ("RETURNED".equalsIgnoreCase(lending.getStatus())) {
            vo.setStatusDesc("已归还");
        } else if (isOverdue) {
            vo.setStatusDesc("已逾期");
        } else {
            vo.setStatusDesc("借出中");
        }

        vo.setRemark(lending.getRemark());
        vo.setCreatedAt(lending.getCreatedAt());
        return vo;
    }
}

