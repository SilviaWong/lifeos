package com.lifeos.business.controller;

import com.lifeos.business.dto.AbortPomodoroRequest;
import com.lifeos.business.dto.FinishPomodoroRequest;
import com.lifeos.business.dto.PomodoroQueryRequest;
import com.lifeos.business.dto.StartPomodoroRequest;
import com.lifeos.business.dto.UpdatePomodoroSettingRequest;
import com.lifeos.business.service.PomodoroService;
import com.lifeos.business.vo.PomodoroRecordVO;
import com.lifeos.business.vo.PomodoroSettingVO;
import com.lifeos.business.vo.PomodoroSummaryVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 番茄钟与深度专注控制器
 */
@Tag(name = "番茄钟管理", description = "专注计时、暂停/恢复、完成/中止、偏好设置与专注统计")
@RestController
@RequestMapping("/api/pomodoros")
public class PomodoroController {

    private final PomodoroService pomodoroService;

    public PomodoroController(PomodoroService pomodoroService) {
        this.pomodoroService = pomodoroService;
    }

    @Operation(summary = "开启一轮番茄钟会话")
    @PostMapping("/start")
    public ApiResponse<PomodoroRecordVO> startSession(@Valid @RequestBody StartPomodoroRequest request) {
        PomodoroRecordVO vo = pomodoroService.startSession(request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "暂停当前进行中的会话")
    @PostMapping("/{id}/pause")
    public ApiResponse<PomodoroRecordVO> pauseSession(
            @Parameter(description = "会话 ID", required = true) @PathVariable("id") Long id) {
        PomodoroRecordVO vo = pomodoroService.pauseSession(id);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "恢复已暂停的会话")
    @PostMapping("/{id}/resume")
    public ApiResponse<PomodoroRecordVO> resumeSession(
            @Parameter(description = "会话 ID", required = true) @PathVariable("id") Long id) {
        PomodoroRecordVO vo = pomodoroService.resumeSession(id);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "完成番茄钟专注会话")
    @PostMapping("/{id}/finish")
    public ApiResponse<PomodoroRecordVO> finishSession(
            @Parameter(description = "会话 ID", required = true) @PathVariable("id") Long id,
            @RequestBody(required = false) FinishPomodoroRequest request) {
        PomodoroRecordVO vo = pomodoroService.finishSession(id, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "中途放弃/中止番茄钟会话")
    @PostMapping("/{id}/abort")
    public ApiResponse<PomodoroRecordVO> abortSession(
            @Parameter(description = "会话 ID", required = true) @PathVariable("id") Long id,
            @RequestBody(required = false) AbortPomodoroRequest request) {
        PomodoroRecordVO vo = pomodoroService.abortSession(id, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "获取当前进行中或已暂停的会话")
    @GetMapping("/current")
    public ApiResponse<PomodoroRecordVO> getCurrentSession() {
        PomodoroRecordVO vo = pomodoroService.getCurrentSession();
        return ApiResponse.success(vo);
    }

    @Operation(summary = "获取指定会话详情")
    @GetMapping("/{id}")
    public ApiResponse<PomodoroRecordVO> getSessionById(
            @Parameter(description = "会话 ID", required = true) @PathVariable("id") Long id) {
        PomodoroRecordVO vo = pomodoroService.getSessionById(id);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "分页查询番茄钟历史记录")
    @GetMapping("/records")
    public ApiResponse<PageResult<PomodoroRecordVO>> queryRecords(@Valid PomodoroQueryRequest request) {
        PageResult<PomodoroRecordVO> page = pomodoroService.queryRecords(request);
        return ApiResponse.success(page);
    }

    @Operation(summary = "获取番茄钟专注统计摘要")
    @GetMapping("/summary")
    public ApiResponse<PomodoroSummaryVO> getSummary() {
        PomodoroSummaryVO summary = pomodoroService.getSummary();
        return ApiResponse.success(summary);
    }

    @Operation(summary = "获取番茄钟用户偏好配置")
    @GetMapping("/settings")
    public ApiResponse<PomodoroSettingVO> getSetting() {
        PomodoroSettingVO setting = pomodoroService.getSetting();
        return ApiResponse.success(setting);
    }

    @Operation(summary = "修改番茄钟用户偏好配置")
    @PutMapping("/settings")
    public ApiResponse<PomodoroSettingVO> updateSetting(@Valid @RequestBody UpdatePomodoroSettingRequest request) {
        PomodoroSettingVO setting = pomodoroService.updateSetting(request);
        return ApiResponse.success(setting);
    }
}

