package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateItemRequest;
import com.lifeos.business.dto.ItemQueryRequest;
import com.lifeos.business.dto.UpdateItemRequest;
import com.lifeos.business.entity.Diary;
import com.lifeos.business.entity.ItemLending;
import com.lifeos.business.entity.Memo;
import com.lifeos.business.entity.PersonalItem;
import com.lifeos.business.mapper.DiaryMapper;
import com.lifeos.business.mapper.ItemLendingMapper;
import com.lifeos.business.mapper.MemoMapper;
import com.lifeos.business.mapper.PersonalItemMapper;
import com.lifeos.business.service.ItemLendingService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.service.PersonalItemService;
import com.lifeos.business.vo.ItemDetailVO;
import com.lifeos.business.vo.ItemLendingVO;
import com.lifeos.business.vo.LifeRecordSummaryVO;
import com.lifeos.business.vo.PersonalItemVO;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个人物品管理服务实现
 */
@Service
public class PersonalItemServiceImpl implements PersonalItemService {

    private final PersonalItemMapper personalItemMapper;
    private final ItemLendingMapper itemLendingMapper;
    private final MemoMapper memoMapper;
    private final DiaryMapper diaryMapper;
    private final ModuleUsageLogService moduleUsageLogService;
    private final ItemLendingService itemLendingService;

    public PersonalItemServiceImpl(PersonalItemMapper personalItemMapper,
                                   ItemLendingMapper itemLendingMapper,
                                   MemoMapper memoMapper,
                                   DiaryMapper diaryMapper,
                                   ModuleUsageLogService moduleUsageLogService,
                                   @Lazy ItemLendingService itemLendingService) {
        this.personalItemMapper = personalItemMapper;
        this.itemLendingMapper = itemLendingMapper;
        this.memoMapper = memoMapper;
        this.diaryMapper = diaryMapper;
        this.moduleUsageLogService = moduleUsageLogService;
        this.itemLendingService = itemLendingService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PersonalItemVO createItem(CreateItemRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        PersonalItem item = new PersonalItem();
        item.setUserId(userId);
        item.setItemName(request.getItemName().trim());
        item.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory().trim() : "生活用品");
        item.setQuantity(request.getQuantity() != null ? request.getQuantity() : BigDecimal.ONE);
        item.setUnit(StringUtils.hasText(request.getUnit()) ? request.getUnit().trim() : "件");
        item.setStorageLocation(request.getStorageLocation());
        item.setPurchaseDate(request.getPurchaseDate());
        item.setStatus("ACTIVE");
        item.setRemark(request.getRemark());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setDeleted(0);

        personalItemMapper.insert(item);
        moduleUsageLogService.logUsage(userId, "ITEM", "CREATE", item.getId());

        return convertToVO(item, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PersonalItemVO updateItem(Long id, UpdateItemRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        PersonalItem item = getValidItem(id, userId);

        item.setItemName(request.getItemName().trim());
        if (StringUtils.hasText(request.getCategory())) {
            item.setCategory(request.getCategory().trim());
        }
        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (StringUtils.hasText(request.getUnit())) {
            item.setUnit(request.getUnit().trim());
        }
        item.setStorageLocation(request.getStorageLocation());
        item.setPurchaseDate(request.getPurchaseDate());
        if (StringUtils.hasText(request.getStatus())) {
            item.setStatus(request.getStatus().trim().toUpperCase());
        }
        item.setRemark(request.getRemark());
        item.setUpdatedAt(LocalDateTime.now());

        personalItemMapper.updateById(item);
        moduleUsageLogService.logUsage(userId, "ITEM", "UPDATE", item.getId());

        ItemLending activeLending = findActiveLending(item.getId(), userId);
        return convertToVO(item, activeLending);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        PersonalItem item = getValidItem(id, userId);

        // 校验是否有借出中的记录
        ItemLending activeLending = findActiveLending(id, userId);
        if (activeLending != null) {
            throw new BusinessException(400, "该物品当前正处于借出中状态，不能删除，请先登记归还");
        }

        personalItemMapper.deleteById(item.getId());
        moduleUsageLogService.logUsage(userId, "ITEM", "DELETE", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PersonalItemVO toggleArchive(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        PersonalItem item = getValidItem(id, userId);

        String newStatus = "ACTIVE".equalsIgnoreCase(item.getStatus()) ? "ARCHIVED" : "ACTIVE";
        item.setStatus(newStatus);
        item.setUpdatedAt(LocalDateTime.now());
        personalItemMapper.updateById(item);

        moduleUsageLogService.logUsage(userId, "ITEM", "ARCHIVE", id);
        ItemLending activeLending = findActiveLending(item.getId(), userId);
        return convertToVO(item, activeLending);
    }

    @Override
    public ItemDetailVO getItemDetail(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        PersonalItem item = getValidItem(id, userId);

        ItemLending activeLending = findActiveLending(id, userId);
        PersonalItemVO itemVO = convertToVO(item, activeLending);

        List<ItemLendingVO> history = itemLendingService.getLendingsByItem(id);
        ItemLendingVO activeVO = activeLending != null ? convertLendingToVO(activeLending, item.getItemName()) : null;

        return new ItemDetailVO(itemVO, activeVO, history);
    }

    @Override
    public PageResult<PersonalItemVO> queryItems(ItemQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        if (request == null) {
            request = new ItemQueryRequest();
        }

        Page<PersonalItem> pageParam = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<PersonalItem> qw = new LambdaQueryWrapper<>();
        qw.eq(PersonalItem::getUserId, userId);
        qw.eq(PersonalItem::getDeleted, 0);

        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            qw.and(wrapper -> wrapper.like(PersonalItem::getItemName, kw)
                    .or().like(PersonalItem::getStorageLocation, kw)
                    .or().like(PersonalItem::getRemark, kw));
        }

        if (StringUtils.hasText(request.getCategory())) {
            qw.eq(PersonalItem::getCategory, request.getCategory().trim());
        }

        if (StringUtils.hasText(request.getStatus())) {
            qw.eq(PersonalItem::getStatus, request.getStatus().trim().toUpperCase());
        }

        qw.orderByDesc(PersonalItem::getUpdatedAt);

        Page<PersonalItem> pageResult = personalItemMapper.selectPage(pageParam, qw);
        List<PersonalItem> records = pageResult.getRecords();

        if (records == null || records.isEmpty()) {
            return PageResult.empty((int) pageResult.getCurrent(), (int) pageResult.getSize());
        }

        // 批量查询所有处于借出状态（LENT, OVERDUE）的记录
        Set<Long> itemIds = records.stream().map(PersonalItem::getId).collect(Collectors.toSet());
        List<ItemLending> activeLendings = itemLendingMapper.selectList(
                new LambdaQueryWrapper<ItemLending>()
                        .eq(ItemLending::getUserId, userId)
                        .in(ItemLending::getItemId, itemIds)
                        .in(ItemLending::getStatus, Arrays.asList("LENT", "OVERDUE"))
        );

        Map<Long, ItemLending> activeMap = activeLendings.stream()
                .collect(Collectors.toMap(ItemLending::getItemId, l -> l, (l1, l2) -> l1));

        List<PersonalItemVO> voList = new ArrayList<>();
        for (PersonalItem item : records) {
            ItemLending lending = activeMap.get(item.getId());
            boolean lent = (lending != null);

            if (request.getIsLent() != null && request.getIsLent() != lent) {
                continue;
            }

            voList.add(convertToVO(item, lending));
        }

        return PageResult.of(voList, pageResult.getTotal(), (int) pageResult.getCurrent(), (int) pageResult.getSize());
    }

    @Override
    public LifeRecordSummaryVO getLifeRecordSummary() {
        Long userId = SecurityUtil.getLoginUserId();
        LifeRecordSummaryVO summary = new LifeRecordSummaryVO();

        // 物品与借还
        long totalItems = personalItemMapper.selectCount(
                new LambdaQueryWrapper<PersonalItem>().eq(PersonalItem::getUserId, userId).eq(PersonalItem::getDeleted, 0)
        );
        summary.setTotalItemCount(totalItems);

        List<ItemLending> unreturnedList = itemLendingMapper.selectList(
                new LambdaQueryWrapper<ItemLending>()
                        .eq(ItemLending::getUserId, userId)
                        .in(ItemLending::getStatus, Arrays.asList("LENT", "OVERDUE"))
        );
        summary.setLentItemCount((long) unreturnedList.size());

        LocalDateTime now = LocalDateTime.now();
        long overdueCount = unreturnedList.stream()
                .filter(l -> l.getExpectedReturnAt() != null && l.getExpectedReturnAt().isBefore(now))
                .count();
        summary.setOverdueLendingCount(overdueCount);

        // 备忘录
        long totalMemos = memoMapper.selectCount(
                new LambdaQueryWrapper<Memo>().eq(Memo::getUserId, userId).eq(Memo::getDeleted, 0)
        );
        summary.setTotalMemoCount(totalMemos);

        long pinnedMemos = memoMapper.selectCount(
                new LambdaQueryWrapper<Memo>().eq(Memo::getUserId, userId).eq(Memo::getDeleted, 0).eq(Memo::getIsPinned, 1)
        );
        summary.setPinnedMemoCount(pinnedMemos);

        // 日记
        long totalDiaries = diaryMapper.selectCount(
                new LambdaQueryWrapper<Diary>().eq(Diary::getUserId, userId).eq(Diary::getDeleted, 0)
        );
        summary.setTotalDiaryCount(totalDiaries);

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        long thisMonthDiaries = diaryMapper.selectCount(
                new LambdaQueryWrapper<Diary>()
                        .eq(Diary::getUserId, userId)
                        .eq(Diary::getDeleted, 0)
                        .ge(Diary::getDiaryDate, startOfMonth)
        );
        summary.setThisMonthDiaryCount(thisMonthDiaries);

        return summary;
    }

    private PersonalItem getValidItem(Long id, Long userId) {
        PersonalItem item = personalItemMapper.selectById(id);
        if (item == null || item.getDeleted() != 0 || !item.getUserId().equals(userId)) {
            throw new BusinessException(404, "物品不存在或无权访问");
        }
        return item;
    }

    private ItemLending findActiveLending(Long itemId, Long userId) {
        return itemLendingMapper.selectOne(
                new LambdaQueryWrapper<ItemLending>()
                        .eq(ItemLending::getUserId, userId)
                        .eq(ItemLending::getItemId, itemId)
                        .in(ItemLending::getStatus, Arrays.asList("LENT", "OVERDUE"))
                        .last("LIMIT 1")
        );
    }

    private PersonalItemVO convertToVO(PersonalItem item, ItemLending lending) {
        PersonalItemVO vo = new PersonalItemVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());
        vo.setItemName(item.getItemName());
        vo.setCategory(item.getCategory());
        vo.setQuantity(item.getQuantity());
        vo.setUnit(item.getUnit());
        vo.setStorageLocation(item.getStorageLocation());
        vo.setPurchaseDate(item.getPurchaseDate());
        vo.setStatus(item.getStatus());
        vo.setStatusDesc("ARCHIVED".equalsIgnoreCase(item.getStatus()) ? "已归档" : "正常在用");
        vo.setRemark(item.getRemark());
        vo.setCreatedAt(item.getCreatedAt());
        vo.setUpdatedAt(item.getUpdatedAt());

        if (lending != null) {
            vo.setIsLent(true);
            vo.setCurrentLendingId(lending.getId());
            vo.setCurrentBorrower(lending.getBorrowerName());
            vo.setCurrentLentAt(lending.getLentAt());
            vo.setCurrentExpectedReturnAt(lending.getExpectedReturnAt());
            boolean overdue = lending.getExpectedReturnAt() != null && lending.getExpectedReturnAt().isBefore(LocalDateTime.now());
            vo.setIsOverdue(overdue);
        } else {
            vo.setIsLent(false);
            vo.setIsOverdue(false);
        }

        return vo;
    }

    private ItemLendingVO convertLendingToVO(ItemLending lending, String itemName) {
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

