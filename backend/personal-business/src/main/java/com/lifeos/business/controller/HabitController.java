package com.lifeos.business.controller;

import com.lifeos.business.dto.*;
import com.lifeos.business.service.HabitService;
import com.lifeos.business.vo.HabitCalendarVO;
import com.lifeos.business.vo.HabitCheckinVO;
import com.lifeos.business.vo.HabitSummaryVO;
import com.lifeos.business.vo.HabitVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 习惯打卡控制器
 */
@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    /**
     * 新增习惯
     */
    @PostMapping
    public ApiResponse<HabitVO> createHabit(@RequestBody @Valid CreateHabitRequest request) {
        return ApiResponse.success(habitService.createHabit(request));
    }

    /**
     * 编辑习惯
     */
    @PutMapping("/{id}")
    public ApiResponse<HabitVO> updateHabit(@PathVariable Long id,
                                            @RequestBody @Valid UpdateHabitRequest request) {
        return ApiResponse.success(habitService.updateHabit(id, request));
    }

    /**
     * 更新习惯状态（暂停、恢复、归档）
     */
    @PutMapping("/{id}/status")
    public ApiResponse<HabitVO> updateHabitStatus(@PathVariable Long id,
                                                  @RequestBody @Valid UpdateHabitStatusRequest request) {
        return ApiResponse.success(habitService.updateHabitStatus(id, request));
    }

    /**
     * 删除习惯
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHabit(@PathVariable Long id) {
        habitService.deleteHabit(id);
        return ApiResponse.success();
    }

    /**
     * 获取习惯详情
     */
    @GetMapping("/{id}")
    public ApiResponse<HabitVO> getHabitById(@PathVariable Long id) {
        return ApiResponse.success(habitService.getHabitById(id));
    }

    /**
     * 查询习惯列表
     */
    @GetMapping
    public ApiResponse<List<HabitVO>> getHabitList(HabitQueryRequest request) {
        return ApiResponse.success(habitService.getHabitList(request));
    }

    /**
     * 习惯总览统计
     */
    @GetMapping("/summary")
    public ApiResponse<HabitSummaryVO> getHabitSummary() {
        return ApiResponse.success(habitService.getHabitSummary());
    }

    /**
     * 习惯打卡
     */
    @PostMapping("/{id}/checkins")
    public ApiResponse<HabitVO> checkin(@PathVariable Long id,
                                        @RequestBody(required = false) CheckinHabitRequest request) {
        return ApiResponse.success(habitService.checkin(id, request));
    }

    /**
     * 按指定日期取消打卡
     */
    @DeleteMapping("/{id}/checkins/{date}")
    public ApiResponse<HabitVO> cancelCheckinByDate(@PathVariable Long id,
                                                    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(habitService.cancelCheckin(id, date));
    }

    /**
     * 取消打卡（缺省当天或查询参数 date）
     */
    @DeleteMapping("/{id}/checkins")
    public ApiResponse<HabitVO> cancelCheckin(@PathVariable Long id,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(habitService.cancelCheckin(id, date));
    }

    /**
     * 获取习惯月历打卡数据
     */
    @GetMapping("/{id}/calendar")
    public ApiResponse<HabitCalendarVO> getHabitCalendar(@PathVariable Long id,
                                                         @RequestParam(required = false) Integer year,
                                                         @RequestParam(required = false) Integer month) {
        return ApiResponse.success(habitService.getHabitCalendar(id, year, month));
    }

    /**
     * 分页查询打卡记录明细
     */
    @GetMapping("/{id}/checkins")
    public ApiResponse<PageResult<HabitCheckinVO>> getHabitCheckins(@PathVariable Long id,
                                                                    @RequestParam(defaultValue = "1") Integer pageNo,
                                                                    @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(habitService.getHabitCheckins(id, pageNo, pageSize));
    }
}
