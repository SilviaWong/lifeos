package com.lifeos.system.controller;

import com.lifeos.common.api.ApiResponse;
import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import com.lifeos.system.service.AuthService;
import com.lifeos.system.service.UserService;
import com.lifeos.system.vo.AuthTokenVO;
import com.lifeos.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号认证与登录控制器
 */
@Tag(name = "认证中心", description = "用户注册、账号登录、退出与当前登录态接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserVO userVO = authService.register(registerDTO);
        return ApiResponse.success("注册成功", userVO);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<AuthTokenVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthTokenVO tokenVO = authService.login(loginDTO);
        return ApiResponse.success("登录成功", tokenVO);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success("退出登录成功", null);
    }

    @Operation(summary = "获取当前已登录用户信息")
    @GetMapping("/me")
    public ApiResponse<UserVO> getMyInfo() {
        UserVO userVO = userService.getCurrentUser();
        return ApiResponse.success(userVO);
    }
}

