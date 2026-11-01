package com.lifeos.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeos.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联持久层 Mapper
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}

