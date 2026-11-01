package com.lifeos.business.service;

import com.lifeos.business.dto.CreateTodoRequest;
import com.lifeos.business.dto.TodoQueryRequest;
import com.lifeos.business.dto.UpdateTodoRequest;
import com.lifeos.business.dto.UpdateTodoStatusRequest;
import com.lifeos.business.vo.TodoSummaryVO;
import com.lifeos.business.vo.TodoVO;
import com.lifeos.common.api.PageResult;

/**
 * 待办任务业务服务接口
 */
public interface TodoTaskService {

    /**
     * 创建待办任务
     *
     * @param request 创建请求参数
     * @return 创建后的待办信息
     */
    TodoVO createTodo(CreateTodoRequest request);

    /**
     * 更新待办任务基础信息
     *
     * @param id      待办任务 ID
     * @param request 更新请求参数
     * @return 更新后的待办信息
     */
    TodoVO updateTodo(Long id, UpdateTodoRequest request);

    /**
     * 变更待办任务状态
     *
     * @param id      待办任务 ID
     * @param request 状态变更请求参数
     * @return 变更后的待办信息
     */
    TodoVO updateTodoStatus(Long id, UpdateTodoStatusRequest request);

    /**
     * 删除待办任务（软删除）
     *
     * @param id 待办任务 ID
     */
    void deleteTodo(Long id);

    /**
     * 查询指定待办详情
     *
     * @param id 待办任务 ID
     * @return 待办详情
     */
    TodoVO getTodoById(Long id);

    /**
     * 分页多维度条件查询待办任务列表
     *
     * @param request 查询与筛选参数
     * @return 分页结果
     */
    PageResult<TodoVO> queryTodos(TodoQueryRequest request);

    /**
     * 获取当前登录用户的待办统计摘要
     *
     * @return 待办统计摘要
     */
    TodoSummaryVO getTodoSummary();
}

