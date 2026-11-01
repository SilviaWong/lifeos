package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.Memo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 个人备忘录持久层 Mapper
 */
@Mapper
public interface MemoMapper extends BaseMapper<Memo> {
}

