package com.lifeos.business.controller;

import com.lifeos.business.dto.CreateTodoRequest;
import com.lifeos.business.dto.TodoQueryRequest;
import com.lifeos.business.dto.UpdateTodoRequest;
import com.lifeos.business.dto.UpdateTodoStatusRequest;
import com.lifeos.business.service.TodoTaskService;
import com.lifeos.business.vo.TodoSummaryVO;
import com.lifeos.business.vo.TodoVO;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * 日程待办任务控制器
 */
@Tag(name = "待办管理", description = "用户日程待办增删改查、状态流转与专项统计")
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoTaskService todoTaskService;

    public TodoController(TodoTaskService todoTaskService) {
        this.todoTaskService = todoTaskService;
    }

    @Operation(summary = "创建待办任务")
    @PostMapping
    public ApiResponse<TodoVO> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        TodoVO vo = todoTaskService.createTodo(request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "分页查询待办列表")
    @GetMapping
    public ApiResponse<PageResult<TodoVO>> queryTodos(@Valid TodoQueryRequest request) {
        PageResult<TodoVO> page = todoTaskService.queryTodos(request);
        return ApiResponse.success(page);
    }

    @Operation(summary = "获取待办统计摘要")
    @GetMapping("/summary")
    public ApiResponse<TodoSummaryVO> getTodoSummary() {
        TodoSummaryVO summary = todoTaskService.getTodoSummary();
        return ApiResponse.success(summary);
    }

    @Operation(summary = "获取指定待办任务详情")
    @GetMapping("/{id}")
    public ApiResponse<TodoVO> getTodoById(@Parameter(description = "待办 ID") @PathVariable("id") Long id) {
        TodoVO vo = todoTaskService.getTodoById(id);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "更新待办任务")
    @PutMapping("/{id}")
    public ApiResponse<TodoVO> updateTodo(
            @Parameter(description = "待办 ID") @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTodoRequest request) {
        TodoVO vo = todoTaskService.updateTodo(id, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "更新待办状态（完成、取消、重新打开）")
    @PatchMapping("/{id}/status")
    public ApiResponse<TodoVO> updateTodoStatus(
            @Parameter(description = "待办 ID") @PathVariable("id") Long id,
            @Valid @RequestBody UpdateTodoStatusRequest request) {
        TodoVO vo = todoTaskService.updateTodoStatus(id, request);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "删除待办任务（软删除）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTodo(@Parameter(description = "待办 ID") @PathVariable("id") Long id) {
        todoTaskService.deleteTodo(id);
        return ApiResponse.success();
    }
}

