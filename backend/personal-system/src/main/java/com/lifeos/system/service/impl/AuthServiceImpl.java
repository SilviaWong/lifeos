package com.lifeos.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import com.lifeos.system.entity.SysRole;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.entity.SysUserRole;
import com.lifeos.system.mapper.SysRoleMapper;
import com.lifeos.system.mapper.SysUserMapper;
import com.lifeos.system.mapper.SysUserRoleMapper;
import com.lifeos.system.security.JwtTokenProvider;
import com.lifeos.system.security.SecurityUtil;
import com.lifeos.system.service.AuthService;
import com.lifeos.system.vo.AuthTokenVO;
import com.lifeos.system.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 认证与授权业务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           SysRoleMapper sysRoleMapper,
                           SysUserRoleMapper sysUserRoleMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();

        // 1. 校验用户名是否已存在
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.RESOURCE_ALREADY_EXISTS, "用户名已存在，请更换");
        }

        // 2. 空字符串转换为空值，避免 MySQL UNIQUE 唯一约束因多条空字符串冲突
        String phone = StringUtils.hasText(registerDTO.getPhone()) ? registerDTO.getPhone().trim() : null;
        String email = StringUtils.hasText(registerDTO.getEmail()) ? registerDTO.getEmail().trim() : null;

        if (phone != null) {
            Long phoneCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, phone));
            if (phoneCount != null && phoneCount > 0) {
                throw new BusinessException(CommonErrorCode.RESOURCE_ALREADY_EXISTS, "手机号码已被注册");
            }
        }

        if (email != null) {
            Long emailCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, email));
            if (emailCount != null && emailCount > 0) {
                throw new BusinessException(CommonErrorCode.RESOURCE_ALREADY_EXISTS, "电子邮箱已被注册");
            }
        }

        // 3. 构建用户实体，密码进行 BCrypt 加密存储
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(StringUtils.hasText(registerDTO.getNickname()) ? registerDTO.getNickname() : username);
        user.setPhone(phone);
        user.setEmail(email);
        user.setGender(registerDTO.getGender() != null ? registerDTO.getGender() : 0);
        user.setStatus(SysUser.STATUS_ENABLED);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);

        sysUserMapper.insert(user);

        // 4. 分配默认普通用户角色 USER
        SysRole defaultRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, SysRole.ROLE_USER));
        List<String> roleCodes;
        if (defaultRole != null) {
            sysUserRoleMapper.insert(new SysUserRole(user.getId(), defaultRole.getId()));
            roleCodes = List.of(defaultRole.getRoleCode());
        } else {
            roleCodes = Collections.singletonList(SysRole.ROLE_USER);
        }

        log.info("User registered successfully: userId={}, username={}", user.getId(), user.getUsername());
        return toUserVO(user, roleCodes);
    }

    @Override
    public AuthTokenVO login(LoginDTO loginDTO) {
        String account = loginDTO.getUsername();

        // 1. 根据用户名或手机号查询用户
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, account)
                .or()
                .eq(SysUser::getPhone, account));

        if (user == null) {
            // 防枚举攻击，统一提示模糊错误
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "用户名或密码错误");
        }

        // 2. 检查用户账号是否被禁用
        if (user.getStatus() != null && user.getStatus() == SysUser.STATUS_DISABLED) {
            log.warn("Login attempt for disabled account: userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(CommonErrorCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "用户名或密码错误");
        }

        // 4. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 5. 查询角色并签发 JWT
        List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(user.getId());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roleCodes);

        UserVO userVO = toUserVO(user, roleCodes);
        long expiresIn = jwtTokenProvider.getJwtProperties().getExpiration();

        log.info("User logged in successfully: userId={}, username={}", user.getId(), user.getUsername());
        return new AuthTokenVO(token, "Bearer", expiresIn, userVO);
    }

    @Override
    public void logout() {
        try {
            Long userId = SecurityUtil.getLoginUserId();
            log.info("User logged out: userId={}", userId);
        } catch (Exception ignored) {
        }
    }

    public static UserVO toUserVO(SysUser user, List<String> roleCodes) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setGender(user.getGender());
        vo.setStatus(user.getStatus());
        vo.setRoles(roleCodes != null ? roleCodes : Collections.emptyList());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}

