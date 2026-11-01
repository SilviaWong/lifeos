package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.ReminderRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提醒规则数据访问层
 */
@Mapper
public interface ReminderRuleMapper extends BaseMapper<ReminderRule> {
}

