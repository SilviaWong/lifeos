package com.lifeos.business.controller;

import com.lifeos.business.dto.CreateDiaryRequest;
import com.lifeos.business.dto.DiaryQueryRequest;
import com.lifeos.business.dto.UpdateDiaryRequest;
import com.lifeos.business.service.DiaryService;
import com.lifeos.business.vo.DiaryVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人心情日记接口控制器
 * 严格保护用户隐私，日记正文仅当前本人可读写
 */
@RestController
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /**
     * 写日记
     */
    @PostMapping
    public ApiResponse<DiaryVO> createDiary(@Valid @RequestBody CreateDiaryRequest request) {
        return ApiResponse.success(diaryService.createDiary(request));
    }

    /**
     * 修改日记
     */
    @PutMapping("/{id}")
    public ApiResponse<DiaryVO> updateDiary(@PathVariable Long id,
                                            @Valid @RequestBody UpdateDiaryRequest request) {
        return ApiResponse.success(diaryService.updateDiary(id, request));
    }

    /**
     * 删除日记
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDiary(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        return ApiResponse.success();
    }

    /**
     * 获取单篇日记详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DiaryVO> getDiary(@PathVariable Long id) {
        return ApiResponse.success(diaryService.getDiary(id));
    }

    /**
     * 分页查询日记列表
     */
    @GetMapping
    public ApiResponse<PageResult<DiaryVO>> queryDiaries(DiaryQueryRequest request) {
        return ApiResponse.success(diaryService.queryDiaries(request));
    }
}

