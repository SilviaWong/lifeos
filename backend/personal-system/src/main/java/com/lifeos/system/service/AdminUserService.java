package com.lifeos.system.service;

import com.lifeos.common.api.PageResult;
import com.lifeos.system.dto.AdminUserQueryDTO;
import com.lifeos.system.vo.UserVO;

/**
 * 管理端用户运营管理服务
 */
public interface AdminUserService {

    /**
     * 分页查询平台用户列表
     */
    PageResult<UserVO> getUserPage(AdminUserQueryDTO queryDTO);

    /**
     * 启用或禁用目标用户账号
     */
    void updateUserStatus(Long userId, Integer status);
}

