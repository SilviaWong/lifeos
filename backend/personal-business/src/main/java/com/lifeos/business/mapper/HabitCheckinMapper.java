package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.HabitCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 习惯打卡记录 Mapper 接口
 */
@Mapper
public interface HabitCheckinMapper extends BaseMapper<HabitCheckin> {

    /**
     * 忽略逻辑删除标记，原生查询指定习惯在指定日期的打卡记录（防止唯一键冲突）
     */
    @Select("SELECT * FROM habit_checkin WHERE habit_id = #{habitId} AND checkin_date = #{checkinDate} LIMIT 1")
    HabitCheckin findRawByHabitIdAndCheckinDate(@Param("habitId") Long habitId, @Param("checkinDate") LocalDate checkinDate);

    /**
     * 重新激活已软删除的打卡记录
     */
    @Update("UPDATE habit_checkin SET deleted = 0, note = #{note}, is_makeup = #{isMakeup}, created_at = #{createdAt} WHERE id = #{id}")
    int reactivateCheckin(@Param("id") Long id,
                          @Param("note") String note,
                          @Param("isMakeup") Integer isMakeup,
                          @Param("createdAt") LocalDateTime createdAt);
}

