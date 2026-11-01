package com.lifeos.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.exception.BusinessException;
import com.lifeos.system.dto.AdminUserQueryDTO;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.mapper.SysRoleMapper;
import com.lifeos.system.mapper.SysUserMapper;
import com.lifeos.system.security.SecurityUtil;
import com.lifeos.system.service.AdminUserService;
import com.lifeos.system.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理端用户运营管理服务实现
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    public AdminUserServiceImpl(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public PageResult<UserVO> getUserPage(AdminUserQueryDTO queryDTO) {
        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getUsername())) {
            wrapper.like(SysUser::getUsername, queryDTO.getUsername());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.eq(SysUser::getPhone, queryDTO.getPhone());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);

        Page<SysUser> resultPage = sysUserMapper.selectPage(page, wrapper);

        List<UserVO> voList = new ArrayList<>();
        for (SysUser user : resultPage.getRecords()) {
            List<String> roleCodes = sysRoleMapper.selectRoleCodesByUserId(user.getId());
            // 如果筛选了角色编码，进行内存过滤或在列表结果中包含
            if (!StringUtils.hasText(queryDTO.getRoleCode()) || roleCodes.contains(queryDTO.getRoleCode())) {
                voList.add(AuthServiceImpl.toUserVO(user, roleCodes));
            }
        }

        return PageResult.of(voList, resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        Long currentAdminId = SecurityUtil.getLoginUserId();

        // 防止管理员误操作禁用自身当前登录账号
        if (currentAdminId.equals(userId) && status == SysUser.STATUS_DISABLED) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST, "无法禁用当前登录的管理员自身账号");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || (user.getDeleted() != null && user.getDeleted() == 1)) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND, "目标用户不存在");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        log.info("Admin updated user status: adminId={}, targetUserId={}, status={}", currentAdminId, userId, status);
    }
}

