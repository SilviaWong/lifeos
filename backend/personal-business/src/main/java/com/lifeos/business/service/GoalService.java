package com.lifeos.business.service;

import com.lifeos.business.dto.CreateGoalRequest;
import com.lifeos.business.dto.CreateGoalTaskRequest;
import com.lifeos.business.dto.GoalQueryRequest;
import com.lifeos.business.dto.UpdateGoalRequest;
import com.lifeos.business.dto.UpdateGoalStatusRequest;
import com.lifeos.business.dto.UpdateGoalTaskRequest;
import com.lifeos.business.dto.UpdateGoalTaskStatusRequest;
import com.lifeos.business.vo.GoalSummaryVO;
import com.lifeos.business.vo.GoalTaskVO;
import com.lifeos.business.vo.GoalVO;
import com.lifeos.common.api.PageResult;

import java.util.List;

/**
 * 目标与子任务业务服务接口
 */
public interface GoalService {

    /**
     * 创建新目标
     */
    GoalVO createGoal(CreateGoalRequest request);

    /**
     * 更新目标基本信息
     */
    GoalVO updateGoal(Long id, UpdateGoalRequest request);

    /**
     * 更新目标状态（未开始、进行中、已完成、已归档）
     */
    GoalVO updateGoalStatus(Long id, UpdateGoalStatusRequest request);

    /**
     * 删除目标（软删除目标、软删除关联子任务、解除待办关联不级联删除待办）
     */
    void deleteGoal(Long id);

    /**
     * 获取目标详情
     *
     * @param id           目标 ID
     * @param includeTasks 是否包含子任务列表
     */
    GoalVO getGoalById(Long id, boolean includeTasks);

    /**
     * 分页查询目标列表
     */
    PageResult<GoalVO> queryGoals(GoalQueryRequest request);

    /**
     * 获取当前用户目标统计概要
     */
    GoalSummaryVO getGoalSummary();

    // ==================== 子任务相关 ====================

    /**
     * 为指定目标添加子任务并重算目标进度
     */
    GoalTaskVO createGoalTask(Long goalId, CreateGoalTaskRequest request);

    /**
     * 更新子任务信息
     */
    GoalTaskVO updateGoalTask(Long goalId, Long taskId, UpdateGoalTaskRequest request);

    /**
     * 更新子任务完成状态并重算目标进度
     */
    GoalTaskVO updateGoalTaskStatus(Long goalId, Long taskId, UpdateGoalTaskStatusRequest request);

    /**
     * 删除子任务并重算目标进度
     */
    void deleteGoalTask(Long goalId, Long taskId);

    /**
     * 获取指定目标的所有有效子任务
     */
    List<GoalTaskVO> getGoalTasks(Long goalId);
}

