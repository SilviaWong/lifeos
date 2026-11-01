package com.lifeos.system.service;

import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import com.lifeos.system.vo.AuthTokenVO;
import com.lifeos.system.vo.UserVO;

/**
 * 认证与授权业务接口
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册参数
     * @return 注册成功的用户信息视图
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录账号与密码
     * @return JWT 访问凭据与用户基础信息
     */
    AuthTokenVO login(LoginDTO loginDTO);

    /**
     * 用户退出登录
     */
    void logout();
}

