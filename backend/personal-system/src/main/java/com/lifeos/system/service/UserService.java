package com.lifeos.system.service;

import com.lifeos.system.dto.UpdatePasswordDTO;
import com.lifeos.system.dto.UpdateProfileDTO;
import com.lifeos.system.vo.UserVO;

/**
 * 用户信息与个人资料服务
 */
public interface UserService {

    /**
     * 获取当前已认证登录用户的详细信息
     */
    UserVO getCurrentUser();

    /**
     * 更新当前已登录用户的个人资料
     */
    UserVO updateProfile(UpdateProfileDTO profileDTO);

    /**
     * 修改当前登录用户的密码
     */
    void updatePassword(UpdatePasswordDTO passwordDTO);

    /**
     * 根据用户 ID 查询用户信息（包含跨用户越权数据隔离检查）
     *
     * @param targetUserId 目标用户 ID
     * @return 目标用户信息
     */
    UserVO getUserById(Long targetUserId);
}

