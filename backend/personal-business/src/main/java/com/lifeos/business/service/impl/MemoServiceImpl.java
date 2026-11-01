package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateMemoRequest;
import com.lifeos.business.dto.MemoQueryRequest;
import com.lifeos.business.dto.UpdateMemoRequest;
import com.lifeos.business.entity.Memo;
import com.lifeos.business.mapper.MemoMapper;
import com.lifeos.business.service.MemoService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.MemoVO;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 个人备忘录服务实现
 */
@Service
public class MemoServiceImpl implements MemoService {

    private final MemoMapper memoMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public MemoServiceImpl(MemoMapper memoMapper, ModuleUsageLogService moduleUsageLogService) {
        this.memoMapper = memoMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoVO createMemo(CreateMemoRequest request) {
        Long userId = SecurityUtil.getLoginUserId();

        Memo memo = new Memo();
        memo.setUserId(userId);
        memo.setTitle(request.getTitle().trim());
        memo.setContent(request.getContent());
        memo.setTags(request.getTags());
        memo.setIsPinned(request.getIsPinned() != null && request.getIsPinned() == 1 ? 1 : 0);
        memo.setStatus("ACTIVE");
        memo.setCreatedAt(LocalDateTime.now());
        memo.setUpdatedAt(LocalDateTime.now());
        memo.setDeleted(0);

        memoMapper.insert(memo);
        moduleUsageLogService.logUsage(userId, "MEMO", "CREATE", memo.getId());

        return convertToVO(memo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoVO updateMemo(Long id, UpdateMemoRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Memo memo = getValidMemo(id, userId);

        memo.setTitle(request.getTitle().trim());
        memo.setContent(request.getContent());
        memo.setTags(request.getTags());
        if (request.getIsPinned() != null) {
            memo.setIsPinned(request.getIsPinned() == 1 ? 1 : 0);
        }
        if (StringUtils.hasText(request.getStatus())) {
            memo.setStatus(request.getStatus().trim().toUpperCase());
        }
        memo.setUpdatedAt(LocalDateTime.now());

        memoMapper.updateById(memo);
        moduleUsageLogService.logUsage(userId, "MEMO", "UPDATE", memo.getId());

        return convertToVO(memo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMemo(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Memo memo = getValidMemo(id, userId);

        memoMapper.deleteById(memo.getId());
        moduleUsageLogService.logUsage(userId, "MEMO", "DELETE", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoVO togglePin(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Memo memo = getValidMemo(id, userId);

        int newPinned = memo.getIsPinned() != null && memo.getIsPinned() == 1 ? 0 : 1;
        memo.setIsPinned(newPinned);
        memo.setUpdatedAt(LocalDateTime.now());
        memoMapper.updateById(memo);

        moduleUsageLogService.logUsage(userId, "MEMO", "PIN", id);
        return convertToVO(memo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoVO toggleArchive(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Memo memo = getValidMemo(id, userId);

        String newStatus = "ACTIVE".equalsIgnoreCase(memo.getStatus()) ? "ARCHIVED" : "ACTIVE";
        memo.setStatus(newStatus);
        memo.setUpdatedAt(LocalDateTime.now());
        memoMapper.updateById(memo);

        moduleUsageLogService.logUsage(userId, "MEMO", "ARCHIVE", id);
        return convertToVO(memo);
    }

    @Override
    public MemoVO getMemo(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Memo memo = getValidMemo(id, userId);
        return convertToVO(memo);
    }

    @Override
    public PageResult<MemoVO> queryMemos(MemoQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        if (request == null) {
            request = new MemoQueryRequest();
        }

        Page<Memo> pageParam = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<Memo> qw = new LambdaQueryWrapper<>();
        qw.eq(Memo::getUserId, userId);
        qw.eq(Memo::getDeleted, 0);

        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            qw.and(wrapper -> wrapper.like(Memo::getTitle, kw)
                    .or().like(Memo::getContent, kw)
                    .or().like(Memo::getTags, kw));
        }

        if (StringUtils.hasText(request.getTag())) {
            qw.like(Memo::getTags, request.getTag().trim());
        }

        if (StringUtils.hasText(request.getStatus())) {
            qw.eq(Memo::getStatus, request.getStatus().trim().toUpperCase());
        }

        if (request.getIsPinned() != null) {
            qw.eq(Memo::getIsPinned, request.getIsPinned());
        }

        // 置顶优先，其次按修改时间倒序
        qw.orderByDesc(Memo::getIsPinned)
          .orderByDesc(Memo::getUpdatedAt);

        Page<Memo> pageResult = memoMapper.selectPage(pageParam, qw);
        List<Memo> records = pageResult.getRecords();

        if (records == null || records.isEmpty()) {
            return PageResult.empty((int) pageResult.getCurrent(), (int) pageResult.getSize());
        }

        List<MemoVO> voList = new ArrayList<>(records.size());
        for (Memo m : records) {
            voList.add(convertToVO(m));
        }

        return PageResult.of(voList, pageResult.getTotal(), (int) pageResult.getCurrent(), (int) pageResult.getSize());
    }

    private Memo getValidMemo(Long id, Long userId) {
        Memo memo = memoMapper.selectById(id);
        if (memo == null || memo.getDeleted() != 0 || !memo.getUserId().equals(userId)) {
            throw new BusinessException(404, "备忘录不存在或无权访问");
        }
        return memo;
    }

    private MemoVO convertToVO(Memo memo) {
        MemoVO vo = new MemoVO();
        vo.setId(memo.getId());
        vo.setUserId(memo.getUserId());
        vo.setTitle(memo.getTitle());
        vo.setContent(memo.getContent());
        vo.setTags(memo.getTags());
        vo.setIsPinned(memo.getIsPinned());
        vo.setStatus(memo.getStatus());
        vo.setStatusDesc("ARCHIVED".equalsIgnoreCase(memo.getStatus()) ? "已归档" : "活跃");
        vo.setCreatedAt(memo.getCreatedAt());
        vo.setUpdatedAt(memo.getUpdatedAt());
        return vo;
    }
}
