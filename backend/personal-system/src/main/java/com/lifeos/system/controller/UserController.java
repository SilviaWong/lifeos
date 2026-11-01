package com.lifeos.system.controller;

import com.lifeos.common.api.ApiResponse;
import com.lifeos.system.dto.UpdatePasswordDTO;
import com.lifeos.system.dto.UpdateProfileDTO;
import com.lifeos.system.service.UserService;
import com.lifeos.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户个人中心与资料控制器
 */
@Tag(name = "用户中心", description = "个人资料修改、密码修改与用户信息查询")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "更新当前登录用户个人资料")
    @PutMapping("/me")
    public ApiResponse<UserVO> updateProfile(@Valid @RequestBody UpdateProfileDTO profileDTO) {
        UserVO userVO = userService.updateProfile(profileDTO);
        return ApiResponse.success("个人资料修改成功", userVO);
    }

    @Operation(summary = "修改当前登录用户密码")
    @PutMapping("/me/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO passwordDTO) {
        userService.updatePassword(passwordDTO);
        return ApiResponse.success("密码修改成功，请重新登录", null);
    }

    @Operation(summary = "根据用户ID查询用户（数据越权隔离保护）")
    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUserById(@PathVariable("id") Long id) {
        UserVO userVO = userService.getUserById(id);
        return ApiResponse.success(userVO);
    }
}

