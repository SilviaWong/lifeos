package com.lifeos.business.controller;

import com.lifeos.business.dto.CreateMemoRequest;
import com.lifeos.business.dto.MemoQueryRequest;
import com.lifeos.business.dto.UpdateMemoRequest;
import com.lifeos.business.service.MemoService;
import com.lifeos.business.vo.MemoVO;
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
 * 个人备忘录接口控制器
 */
@RestController
@RequestMapping("/api/memos")
public class MemoController {

    private final MemoService memoService;

    public MemoController(MemoService memoService) {
        this.memoService = memoService;
    }

    /**
     * 新增备忘录
     */
    @PostMapping
    public ApiResponse<MemoVO> createMemo(@Valid @RequestBody CreateMemoRequest request) {
        return ApiResponse.success(memoService.createMemo(request));
    }

    /**
     * 更新备忘录
     */
    @PutMapping("/{id}")
    public ApiResponse<MemoVO> updateMemo(@PathVariable Long id,
                                          @Valid @RequestBody UpdateMemoRequest request) {
        return ApiResponse.success(memoService.updateMemo(id, request));
    }

    /**
     * 删除备忘录
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMemo(@PathVariable Long id) {
        memoService.deleteMemo(id);
        return ApiResponse.success();
    }

    /**
     * 置顶 / 取消置顶
     */
    @PutMapping("/{id}/pin")
    public ApiResponse<MemoVO> togglePin(@PathVariable Long id) {
        return ApiResponse.success(memoService.togglePin(id));
    }

    /**
     * 归档 / 恢复
     */
    @PutMapping("/{id}/archive")
    public ApiResponse<MemoVO> toggleArchive(@PathVariable Long id) {
        return ApiResponse.success(memoService.toggleArchive(id));
    }

    /**
     * 获取备忘录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<MemoVO> getMemo(@PathVariable Long id) {
        return ApiResponse.success(memoService.getMemo(id));
    }

    /**
     * 分页查询备忘录列表
     */
    @GetMapping
    public ApiResponse<PageResult<MemoVO>> queryMemos(MemoQueryRequest request) {
        return ApiResponse.success(memoService.queryMemos(request));
    }
}

