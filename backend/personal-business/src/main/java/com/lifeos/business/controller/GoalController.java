package com.lifeos.business.controller;

import com.lifeos.business.dto.CreateGoalRequest;
import com.lifeos.business.dto.CreateGoalTaskRequest;
import com.lifeos.business.dto.GoalQueryRequest;
import com.lifeos.business.dto.UpdateGoalRequest;
import com.lifeos.business.dto.UpdateGoalStatusRequest;
import com.lifeos.business.dto.UpdateGoalTaskRequest;
import com.lifeos.business.dto.UpdateGoalTaskStatusRequest;
import com.lifeos.business.service.GoalService;
import com.lifeos.business.vo.GoalSummaryVO;
import com.lifeos.business.vo.GoalTaskVO;
import com.lifeos.business.vo.GoalVO;
import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 目标与子任务控制器
 */
@Tag(name = "目标管理", description = "个人生活目标CRUD、子任务拆解与服务端进度动态计算")
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @Operation(summary = "创建新目标")
    @PostMapping
    public ApiResponse<GoalVO> createGoal(@Valid @RequestBody CreateGoalRequest request) {
        GoalVO vo = goalService.createGoal(request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "分页查询目标列表")
    @GetMapping
    public ApiResponse<PageResult<GoalVO>> queryGoals(@Valid GoalQueryRequest request) {
        PageResult<GoalVO> page = goalService.queryGoals(request);
        return ApiResponse.success(page);
    }

    @Operation(summary = "获取目标统计概要")
    @GetMapping("/summary")
    public ApiResponse<GoalSummaryVO> getGoalSummary() {
        GoalSummaryVO summary = goalService.getGoalSummary();
        return ApiResponse.success(summary);
    }

    @Operation(summary = "获取目标详情（包含子任务列表）")
    @GetMapping("/{id}")
    public ApiResponse<GoalVO> getGoalById(@Parameter(description = "目标 ID") @PathVariable("id") Long id) {
        GoalVO vo = goalService.getGoalById(id, true);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "更新目标基本信息")
    @PutMapping("/{id}")
    public ApiResponse<GoalVO> updateGoal(
            @Parameter(description = "目标 ID") @PathVariable("id") Long id,
            @Valid @RequestBody UpdateGoalRequest request) {
        GoalVO vo = goalService.updateGoal(id, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "更新目标状态（未开始、进行中、已完成、已归档）")
    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<GoalVO> updateGoalStatus(
            @Parameter(description = "目标 ID") @PathVariable("id") Long id,
            @Valid @RequestBody UpdateGoalStatusRequest request) {
        GoalVO vo = goalService.updateGoalStatus(id, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "删除目标（软删除子任务，解除关联待办）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGoal(@Parameter(description = "目标 ID") @PathVariable("id") Long id) {
        goalService.deleteGoal(id);
        return ApiResponse.success();
    }

    // ==================== 子任务 API ====================

    @Operation(summary = "为目标创建子任务")
    @PostMapping("/{id}/tasks")
    public ApiResponse<GoalTaskVO> createGoalTask(
            @Parameter(description = "目标 ID") @PathVariable("id") Long id,
            @Valid @RequestBody CreateGoalTaskRequest request) {
        GoalTaskVO vo = goalService.createGoalTask(id, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "获取指定目标的所有子任务")
    @GetMapping("/{id}/tasks")
    public ApiResponse<List<GoalTaskVO>> getGoalTasks(@Parameter(description = "目标 ID") @PathVariable("id") Long id) {
        List<GoalTaskVO> list = goalService.getGoalTasks(id);
        return ApiResponse.success(list);
    }

    @Operation(summary = "修改指定子任务")
    @PutMapping("/{id}/tasks/{taskId}")
    public ApiResponse<GoalTaskVO> updateGoalTask(
            @Parameter(description = "目标 ID") @PathVariable("id") Long id,
            @Parameter(description = "子任务 ID") @PathVariable("taskId") Long taskId,
            @Valid @RequestBody UpdateGoalTaskRequest request) {
        GoalTaskVO vo = goalService.updateGoalTask(id, taskId, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "更新子任务状态（完成/取消完成，自动重算目标进度）")
    @RequestMapping(value = "/{id}/tasks/{taskId}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<GoalTaskVO> updateGoalTaskStatus(
            @Parameter(description = "目标 ID") @PathVariable("id") Long id,
            @Parameter(description = "子任务 ID") @PathVariable("taskId") Long taskId,
            @Valid @RequestBody UpdateGoalTaskStatusRequest request) {
        GoalTaskVO vo = goalService.updateGoalTaskStatus(id, taskId, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "删除子任务（自动重算目标进度）")
    @DeleteMapping("/{id}/tasks/{taskId}")
    public ApiResponse<Void> deleteGoalTask(
            @Parameter(description = "目标 ID") @PathVariable("id") Long id,
            @Parameter(description = "子任务 ID") @PathVariable("taskId") Long taskId) {
        goalService.deleteGoalTask(id, taskId);
        return ApiResponse.success();
    }
}

