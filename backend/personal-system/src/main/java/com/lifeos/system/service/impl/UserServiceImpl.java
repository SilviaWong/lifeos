package com.lifeos.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.dto.UpdatePasswordDTO;
import com.lifeos.system.dto.UpdateProfileDTO;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.mapper.SysRoleMapper;
import com.lifeos.system.mapper.SysUserMapper;
import com.lifeos.system.security.SecurityUtil;
import com.lifeos.system.service.UserService;
import com.lifeos.system.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息与个人资料服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(SysUserMapper sysUserMapper,
                           SysRoleMapper sysRoleMapper,
                           PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserVO getCurrentUser() {
        Long currentUserId = SecurityUtil.getLoginUserId();
        SysUser user = sysUserMapper.selectById(currentUserId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "当前用户账号不存在或已被删除");
        }

        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(currentUserId);
        return AuthServiceImpl.toUserVO(user, roleCodes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateProfile(UpdateProfileDTO profileDTO) {
        Long currentUserId = SecurityUtil.getLoginUserId();
        SysUser user = sysUserMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        String phone = StringUtils.hasText(profileDTO.getPhone()) ? profileDTO.getPhone().trim() : null;
        String email = StringUtils.hasText(profileDTO.getEmail()) ? profileDTO.getEmail().trim() : null;

        // 校验手机号是否被其他账号占用
        if (phone != null && !phone.equals(user.getPhone())) {
            Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, phone)
                    .ne(SysUser::getId, currentUserId));
            if (count != null && count > 0) {
                throw new BusinessException(CommonErrorCode.RESOURCE_ALREADY_EXISTS, "该手机号码已被其他用户使用");
            }
        }

        // 校验邮箱是否被其他账号占用
        if (email != null && !email.equals(user.getEmail())) {
            Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, email)
                    .ne(SysUser::getId, currentUserId));
            if (count != null && count > 0) {
                throw new BusinessException(CommonErrorCode.RESOURCE_ALREADY_EXISTS, "该电子邮箱已被其他用户使用");
            }
        }

        if (profileDTO.getNickname() != null) {
            user.setNickname(profileDTO.getNickname());
        }
        user.setPhone(phone);
        user.setEmail(email);
        if (profileDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(profileDTO.getAvatarUrl());
        }
        if (profileDTO.getGender() != null) {
            user.setGender(profileDTO.getGender());
        }
        user.setUpdatedAt(LocalDateTime.now());

        sysUserMapper.updateById(user);
        log.info("User profile updated: userId={}", currentUserId);

        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(currentUserId);
        return AuthServiceImpl.toUserVO(user, roleCodes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordDTO passwordDTO) {
        Long currentUserId = SecurityUtil.getLoginUserId();
        SysUser user = sysUserMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        // 校验原密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "原密码不正确");
        }

        // 校验新旧密码是否相同
        if (passwordEncoder.matches(passwordDTO.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "新密码不能与原密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(passwordDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("User password changed successfully: userId={}", currentUserId);
    }

    @Override
    public UserVO getUserById(Long targetUserId) {
        Long currentUserId = SecurityUtil.getLoginUserId();

        // 跨用户数据隔离安全审计：非管理员只能读取本人数据
        if (!currentUserId.equals(targetUserId) && !SecurityUtil.isAdmin()) {
            log.warn("Access denied: userId={} attempted to access other user data: targetUserId={}", currentUserId, targetUserId);
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "无权限读取其他用户资料");
        }

        SysUser user = sysUserMapper.selectById(targetUserId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "目标用户不存在");
        }

        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(targetUserId);
        return AuthServiceImpl.toUserVO(user, roleCodes);
    }
}

