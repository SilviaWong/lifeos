package com.lifeos.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.business.entity.Diary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 个人日记持久层 Mapper
 */
@Mapper
public interface DiaryMapper extends BaseMapper<Diary> {
}

