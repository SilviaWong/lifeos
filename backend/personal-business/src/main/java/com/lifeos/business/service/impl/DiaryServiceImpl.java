package com.lifeos.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.business.dto.CreateDiaryRequest;
import com.lifeos.business.dto.DiaryQueryRequest;
import com.lifeos.business.dto.UpdateDiaryRequest;
import com.lifeos.business.entity.Diary;
import com.lifeos.business.mapper.DiaryMapper;
import com.lifeos.business.service.DiaryService;
import com.lifeos.business.service.ModuleUsageLogService;
import com.lifeos.business.vo.DiaryVO;
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
 * 个人心情日记服务实现
 * 安全重点：严禁将日记正文敏感内容写入系统或业务日志中！
 */
@Service
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;
    private final ModuleUsageLogService moduleUsageLogService;

    public DiaryServiceImpl(DiaryMapper diaryMapper, ModuleUsageLogService moduleUsageLogService) {
        this.diaryMapper = diaryMapper;
        this.moduleUsageLogService = moduleUsageLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiaryVO createDiary(CreateDiaryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        LocalDate targetDate = request.getDiaryDate() != null ? request.getDiaryDate() : LocalDate.now();

        // 校验同一天是否已有日记记录
        Long existingCount = diaryMapper.selectCount(
                new LambdaQueryWrapper<Diary>()
                        .eq(Diary::getUserId, userId)
                        .eq(Diary::getDiaryDate, targetDate)
                        .eq(Diary::getDeleted, 0)
        );
        if (existingCount > 0) {
            throw new BusinessException(400, "您在 " + targetDate + " 已经记录过日记，请在已有日记中编辑补充");
        }

        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setDiaryDate(targetDate);
        diary.setTitle(StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : targetDate + " 的随笔");
        diary.setContent(request.getContent()); // 敏感内容，不打印日志
        diary.setMood(request.getMood());
        diary.setWeather(request.getWeather());
        diary.setCoverUrl(request.getCoverUrl());
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());
        diary.setDeleted(0);

        diaryMapper.insert(diary);

        // 审计日志只记录用户 ID、模块编码和业务主键，严禁记录敏感正文
        moduleUsageLogService.logUsage(userId, "DIARY", "CREATE", diary.getId());

        return convertToVO(diary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiaryVO updateDiary(Long id, UpdateDiaryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        Diary diary = getValidDiary(id, userId);

        LocalDate targetDate = request.getDiaryDate() != null ? request.getDiaryDate() : diary.getDiaryDate();
        if (!targetDate.equals(diary.getDiaryDate())) {
            Long existingCount = diaryMapper.selectCount(
                    new LambdaQueryWrapper<Diary>()
                            .eq(Diary::getUserId, userId)
                            .eq(Diary::getDiaryDate, targetDate)
                            .eq(Diary::getDeleted, 0)
                            .ne(Diary::getId, id)
            );
            if (existingCount > 0) {
                throw new BusinessException(400, "目标日期 " + targetDate + " 已存在日记，请勿跨天覆盖");
            }
        }

        diary.setDiaryDate(targetDate);
        if (StringUtils.hasText(request.getTitle())) {
            diary.setTitle(request.getTitle().trim());
        }
        diary.setContent(request.getContent());
        diary.setMood(request.getMood());
        diary.setWeather(request.getWeather());
        diary.setCoverUrl(request.getCoverUrl());
        diary.setUpdatedAt(LocalDateTime.now());

        diaryMapper.updateById(diary);

        // 审计日志仅记录主键
        moduleUsageLogService.logUsage(userId, "DIARY", "UPDATE", diary.getId());

        return convertToVO(diary);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiary(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Diary diary = getValidDiary(id, userId);

        diaryMapper.deleteById(diary.getId());
        moduleUsageLogService.logUsage(userId, "DIARY", "DELETE", id);
    }

    @Override
    public DiaryVO getDiary(Long id) {
        Long userId = SecurityUtil.getLoginUserId();
        Diary diary = getValidDiary(id, userId);
        return convertToVO(diary);
    }

    @Override
    public PageResult<DiaryVO> queryDiaries(DiaryQueryRequest request) {
        Long userId = SecurityUtil.getLoginUserId();
        if (request == null) {
            request = new DiaryQueryRequest();
        }

        Page<Diary> pageParam = new Page<>(request.getCurrent(), request.getSize());
        LambdaQueryWrapper<Diary> qw = new LambdaQueryWrapper<>();
        qw.eq(Diary::getUserId, userId);
        qw.eq(Diary::getDeleted, 0);

        if (request.getStartDate() != null) {
            qw.ge(Diary::getDiaryDate, request.getStartDate());
        }
        if (request.getEndDate() != null) {
            qw.le(Diary::getDiaryDate, request.getEndDate());
        }
        if (StringUtils.hasText(request.getMood())) {
            qw.eq(Diary::getMood, request.getMood().trim().toUpperCase());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            qw.and(wrapper -> wrapper.like(Diary::getTitle, kw).or().like(Diary::getContent, kw));
        }

        qw.orderByDesc(Diary::getDiaryDate)
          .orderByDesc(Diary::getCreatedAt);

        Page<Diary> pageResult = diaryMapper.selectPage(pageParam, qw);
        List<Diary> records = pageResult.getRecords();

        if (records == null || records.isEmpty()) {
            return PageResult.empty((int) pageResult.getCurrent(), (int) pageResult.getSize());
        }

        List<DiaryVO> voList = new ArrayList<>(records.size());
        for (Diary d : records) {
            voList.add(convertToVO(d));
        }

        return PageResult.of(voList, pageResult.getTotal(), (int) pageResult.getCurrent(), (int) pageResult.getSize());
    }

    private Diary getValidDiary(Long id, Long userId) {
        Diary diary = diaryMapper.selectById(id);
        if (diary == null || diary.getDeleted() != 0 || !diary.getUserId().equals(userId)) {
            throw new BusinessException(404, "日记不存在或无权访问");
        }
        return diary;
    }

    private DiaryVO convertToVO(Diary diary) {
        DiaryVO vo = new DiaryVO();
        vo.setId(diary.getId());
        vo.setUserId(diary.getUserId());
        vo.setDiaryDate(diary.getDiaryDate());
        vo.setTitle(diary.getTitle());
        vo.setContent(diary.getContent());
        vo.setMood(diary.getMood());
        vo.setMoodDesc(getMoodDesc(diary.getMood()));
        vo.setWeather(diary.getWeather());
        vo.setWeatherDesc(getWeatherDesc(diary.getWeather()));
        vo.setCoverUrl(diary.getCoverUrl());
        vo.setCreatedAt(diary.getCreatedAt());
        vo.setUpdatedAt(diary.getUpdatedAt());
        return vo;
    }

    private String getMoodDesc(String mood) {
        if (mood == null) return "未知心情";
        return switch (mood.toUpperCase()) {
            case "HAPPY" -> "开心 😊";
            case "CALM" -> "平静 🌿";
            case "TIRED" -> "疲惫 😴";
            case "SAD" -> "失落 🌧️";
            case "EXCITED" -> "兴奋 🚀";
            default -> mood;
        };
    }

    private String getWeatherDesc(String weather) {
        if (weather == null) return "未知天气";
        return switch (weather.toUpperCase()) {
            case "SUNNY" -> "晴天 ☀️";
            case "CLOUDY" -> "多云 ⛅";
            case "RAINY" -> "阴雨 🌧️";
            case "SNOWY" -> "降雪 ❄️";
            case "WINDY" -> "大风 🍃";
            default -> weather;
        };
    }
}

