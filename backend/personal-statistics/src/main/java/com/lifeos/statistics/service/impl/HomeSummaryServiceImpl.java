package com.lifeos.statistics.service.impl;

import com.lifeos.business.service.GoalService;
import com.lifeos.business.service.TodoTaskService;
import com.lifeos.business.vo.GoalSummaryVO;
import com.lifeos.business.vo.TodoSummaryVO;
import com.lifeos.statistics.service.HomeSummaryService;
import com.lifeos.statistics.vo.HomeSummaryVO;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 用户端首页综合摘要聚合服务实现
 */
@Service
public class HomeSummaryServiceImpl implements HomeSummaryService {

    private final TodoTaskService todoTaskService;
    private final GoalService goalService;

    public HomeSummaryServiceImpl(TodoTaskService todoTaskService, GoalService goalService) {
        this.todoTaskService = todoTaskService;
        this.goalService = goalService;
    }

    @Override
    public HomeSummaryVO getHomeSummary() {
        TodoSummaryVO todoSummary = todoTaskService.getTodoSummary();
        GoalSummaryVO goalSummary = goalService.getGoalSummary();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        HomeSummaryVO vo = new HomeSummaryVO();
        vo.setGreeting(generateGreeting(now.getHour()));
        vo.setCurrentDate(today);
        vo.setDayOfWeekDesc(getDayOfWeekChinese(today.getDayOfWeek()));

        vo.setTodayTodoCount(todoSummary.getTodayTotalCount());
        vo.setTodayCompletedTodoCount(todoSummary.getTodayCompletedCount());
        vo.setPendingTodoCount(todoSummary.getPendingCount());
        vo.setTotalTodoCount(todoSummary.getTotalCount());
        vo.setOverdueTodoCount(todoSummary.getOverdueCount());

        vo.setActiveGoalCount(goalSummary.getActiveCount());

        // 占位字段，后续阶段持续接入真实数据
        vo.setTodayHabitCheckinCount(0L);
        vo.setTodayFocusMinutes(0L);

        return vo;
    }

    private String generateGreeting(int hour) {
        if (hour >= 5 && hour < 12) {
            return "早上好，新的一天充满活力！";
        } else if (hour >= 12 && hour < 14) {
            return "中午好，记得适度休息午餐！";
        } else if (hour >= 14 && hour < 19) {
            return "下午好，保持专注冲刺目标！";
        } else {
            return "晚上好，辛苦了一天，好好放松吧！";
        }
    }

    private String getDayOfWeekChinese(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }
}

