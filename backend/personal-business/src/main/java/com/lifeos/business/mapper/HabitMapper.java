package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.Habit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 习惯 Mapper 接口
 */
@Mapper
public interface HabitMapper extends BaseMapper<Habit> {
}

