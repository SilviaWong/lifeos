package com.lifeos.business.service;

import com.lifeos.business.dto.*;
import com.lifeos.business.vo.HabitCalendarVO;
import com.lifeos.business.vo.HabitCheckinVO;
import com.lifeos.business.vo.HabitSummaryVO;
import com.lifeos.business.vo.HabitVO;
import com.lifeos.common.api.PageResult;

import java.time.LocalDate;
import java.util.List;

/**
 * 习惯打卡业务服务接口
 */
public interface HabitService {

    /**
     * 新增习惯
     */
    HabitVO createHabit(CreateHabitRequest request);

    /**
     * 编辑习惯
     */
    HabitVO updateHabit(Long id, UpdateHabitRequest request);

    /**
     * 更新习惯状态（暂停、恢复、归档）
     */
    HabitVO updateHabitStatus(Long id, UpdateHabitStatusRequest request);

    /**
     * 删除习惯（软删除）
     */
    void deleteHabit(Long id);

    /**
     * 获取习惯详情及统计
     */
    HabitVO getHabitById(Long id);

    /**
     * 查询当前用户习惯列表
     */
    List<HabitVO> getHabitList(HabitQueryRequest request);

    /**
     * 习惯打卡
     */
    HabitVO checkin(Long habitId, CheckinHabitRequest request);

    /**
     * 取消打卡
     */
    HabitVO cancelCheckin(Long habitId, LocalDate checkinDate);

    /**
     * 获取指定月份的月历打卡数据
     */
    HabitCalendarVO getHabitCalendar(Long habitId, Integer year, Integer month);

    /**
     * 分页查询习惯打卡流水
     */
    PageResult<HabitCheckinVO> getHabitCheckins(Long habitId, Integer pageNo, Integer pageSize);

    /**
     * 获取当前用户的习惯打卡总览统计
     */
    HabitSummaryVO getHabitSummary();

    /**
     * 统计指定用户今日打卡总次数（供首页聚合使用）
     */
    long countTodayCheckins(Long userId);
}
