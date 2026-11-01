package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.ItemLending;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品借还记录持久层 Mapper
 */
@Mapper
public interface ItemLendingMapper extends BaseMapper<ItemLending> {
}

