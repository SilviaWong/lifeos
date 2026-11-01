package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.ReminderExecutionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提醒执行幂等日志数据访问层
 */
@Mapper
public interface ReminderExecutionLogMapper extends BaseMapper<ReminderExecutionLog> {
}

