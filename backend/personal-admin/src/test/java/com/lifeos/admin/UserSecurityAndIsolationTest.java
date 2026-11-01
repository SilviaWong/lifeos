package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.system.dto.AdminUserStatusDTO;
import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import com.lifeos.system.dto.UpdatePasswordDTO;
import com.lifeos.system.dto.UpdateProfileDTO;
import com.lifeos.system.entity.SysRole;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.entity.SysUserRole;
import com.lifeos.system.mapper.SysRoleMapper;
import com.lifeos.system.mapper.SysUserMapper;
import com.lifeos.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("用户数据隔离与 RBAC 权限控制测试")
class UserSecurityAndIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO(username, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    private String createAdminUserAndGetToken(String username, String password) throws Exception {
        SysUser admin = new SysUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setNickname("管理员");
        admin.setStatus(SysUser.STATUS_ENABLED);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        admin.setDeleted(0);
        sysUserMapper.insert(admin);

        SysRole adminRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, SysRole.ROLE_ADMIN));
        sysUserRoleMapper.insert(new SysUserRole(admin.getId(), adminRole.getId()));

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO(username, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test
    @DisplayName("测试跨用户数据访问隔离 - 普通用户 A 无法读取用户 B 的个人资料")
    void testCrossUserDataIsolation() throws Exception {
        String tokenUserA = registerAndGetToken("user_alice", "alice_pass");
        String tokenUserB = registerAndGetToken("user_bob", "bob_pass");

        SysUser bob = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "user_bob"));

        // 用户 A 尝试访问用户 B 的 ID，预期返回 HTTP 200 业务错误 403 (无权限读取其他用户资料)
        mockMvc.perform(get("/api/users/" + bob.getId())
                        .header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(403)))
                .andExpect(jsonPath("$.message", containsString("无权限读取其他用户资料")));

        // 用户 B 访问自身的 ID，预期正常返回
        mockMvc.perform(get("/api/users/" + bob.getId())
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.username", is("user_bob")));
    }

    @Test
    @DisplayName("测试修改个人资料与修改密码流程")
    void testUpdateProfileAndPassword() throws Exception {
        String token = registerAndGetToken("update_tester", "init_password");

        // 1. 修改个人资料
        UpdateProfileDTO profileDTO = new UpdateProfileDTO();
        profileDTO.setNickname("超级极客");
        profileDTO.setPhone("13988889999");
        profileDTO.setEmail("geek@example.com");

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.nickname", is("超级极客")))
                .andExpect(jsonPath("$.data.phone", is("13988889999")));

        // 2. 修改密码
        UpdatePasswordDTO pwdDTO = new UpdatePasswordDTO("init_password", "new_password_888");
        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pwdDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));

        // 3. 验证旧密码登录失败，新密码登录成功
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO("update_tester", "init_password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(400)));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO("update_tester", "new_password_888"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));
    }

    @Test
    @DisplayName("测试 RBAC 角色边界与管理端接口访问拦截")
    void testRbacAndAdminEndpoints() throws Exception {
        String normalToken = registerAndGetToken("normal_guy", "pwd123456");
        String adminToken = createAdminUserAndGetToken("platform_admin", "admin_secret");

        // 1. 普通用户访问管理端接口 /api/admin/users -> 必须被 Spring Security 拦截返回 HTTP 403
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + normalToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is(403)))
                .andExpect(jsonPath("$.message", containsString("无权限访问")));

        // 2. 管理员访问管理端接口 /api/admin/users -> 正常返回 200 列表
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)))
                .andExpect(jsonPath("$.data.records", notNullValue()));

        // 3. 管理员禁用普通用户
        SysUser normalUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "normal_guy"));

        AdminUserStatusDTO disableDto = new AdminUserStatusDTO(0);
        mockMvc.perform(put("/api/admin/users/" + normalUser.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disableDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(0)));

        // 4. 被禁用的普通用户尝试登录 -> 预期返回 403 账号已被禁用
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO("normal_guy", "pwd123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(403)))
                .andExpect(jsonPath("$.message", containsString("账号已被禁用")));
    }
}

