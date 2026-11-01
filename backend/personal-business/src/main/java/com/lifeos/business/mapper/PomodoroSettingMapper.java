package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.PomodoroSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 番茄钟偏好设置 Mapper
 */
@Mapper
public interface PomodoroSettingMapper extends BaseMapper<PomodoroSetting> {
}

