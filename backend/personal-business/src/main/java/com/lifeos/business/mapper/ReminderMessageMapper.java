package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.ReminderMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 站内提醒消息数据访问层
 */
@Mapper
public interface ReminderMessageMapper extends BaseMapper<ReminderMessage> {

    /**
     * 统计用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM reminder_message WHERE user_id = #{userId} AND status IN ('PENDING', 'SENT') AND deleted = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 一键将用户的所有未读消息标为已读
     */
    @Update("UPDATE reminder_message SET status = 'READ', read_at = #{readAt} WHERE user_id = #{userId} AND status IN ('PENDING', 'SENT') AND deleted = 0")
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}

