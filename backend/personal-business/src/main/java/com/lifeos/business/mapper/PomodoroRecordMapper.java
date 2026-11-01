package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.PomodoroRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 番茄钟会话记录 Mapper
 */
@Mapper
public interface PomodoroRecordMapper extends BaseMapper<PomodoroRecord> {
}

