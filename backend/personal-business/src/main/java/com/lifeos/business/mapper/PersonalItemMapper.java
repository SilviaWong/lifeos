package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.PersonalItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 个人物品持久层 Mapper
 */
@Mapper
public interface PersonalItemMapper extends BaseMapper<PersonalItem> {
}

