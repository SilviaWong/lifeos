package com.lifeos.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户持久层 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

