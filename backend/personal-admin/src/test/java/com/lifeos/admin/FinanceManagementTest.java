package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.CreateFinanceCategoryRequest;
import com.lifeos.business.dto.CreateFinanceRecordRequest;
import com.lifeos.business.dto.UpdateFinanceRecordRequest;
import com.lifeos.business.entity.FinanceCategory;
import com.lifeos.business.entity.FinanceRecord;
import com.lifeos.business.entity.ModuleUsageLog;
import com.lifeos.business.mapper.FinanceCategoryMapper;
import com.lifeos.business.mapper.FinanceRecordMapper;
import com.lifeos.business.mapper.ModuleUsageLogMapper;
import com.lifeos.system.dto.LoginDTO;
import com.lifeos.system.dto.RegisterDTO;
import com.lifeos.system.entity.SysUser;
import com.lifeos.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("收支记录 CRUD、高精度金额计算、分类类型匹配与多维度统计集成测试")
class FinanceManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FinanceRecordMapper financeRecordMapper;

    @Autowired
    private FinanceCategoryMapper financeCategoryMapper;

    @Autowired
    private ModuleUsageLogMapper moduleUsageLogMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private com.lifeos.system.mapper.SysRoleMapper sysRoleMapper;

    @Autowired
    private com.lifeos.system.mapper.SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        financeRecordMapper.delete(null);
        financeCategoryMapper.delete(null);
    }

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setPhone("139" + System.currentTimeMillis() % 100000000);
        registerDTO.setNickname("记账人" + username);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    private String createAdminUserAndGetToken(String username, String password) throws Exception {
        SysUser admin = new SysUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setNickname("财务管理员");
        admin.setStatus(SysUser.STATUS_ENABLED);
        admin.setCreatedAt(java.time.LocalDateTime.now());
        admin.setUpdatedAt(java.time.LocalDateTime.now());
        admin.setDeleted(0);
        sysUserMapper.insert(admin);

        com.lifeos.system.entity.SysRole adminRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<com.lifeos.system.entity.SysRole>()
                        .eq(com.lifeos.system.entity.SysRole::getRoleCode, com.lifeos.system.entity.SysRole.ROLE_ADMIN)
        );
        sysUserRoleMapper.insert(new com.lifeos.system.entity.SysUserRole(admin.getId(), adminRole.getId()));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test
    @DisplayName("测试收支分类管理：管理员维护系统分类，普通用户创建自定义分类与权限校验")
    void testCategoryManagementAndPermissions() throws Exception {
        String adminToken = createAdminUserAndGetToken("finance_admin", "AdminPass123");
        String userToken = registerAndGetToken("finance_user_cat", "Pass123456");

        // 1. 管理员创建系统分类：餐饮美食 (EXPENSE)
        CreateFinanceCategoryRequest sysCat = new CreateFinanceCategoryRequest();
        sysCat.setCategoryName("餐饮美食");
        sysCat.setCategoryType("EXPENSE");
        sysCat.setIcon("food");
        sysCat.setSortNo(10);

        MvcResult sysCatResult = mockMvc.perform(post("/api/admin/finance-categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sysCat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.categoryName").value("餐饮美食"))
                .andExpect(jsonPath("$.data.system").value(true))
                .andReturn();

        long sysCatId = objectMapper.readTree(sysCatResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. 普通用户访问管理员分类管理接口应被拒绝 (403 Forbidden)
        mockMvc.perform(post("/api/admin/finance-categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sysCat)))
                .andExpect(status().isForbidden());

        // 3. 普通用户创建自定义分类：数码硬件 (EXPENSE)
        CreateFinanceCategoryRequest customCat = new CreateFinanceCategoryRequest();
        customCat.setCategoryName("数码硬件");
        customCat.setCategoryType("EXPENSE");
        customCat.setIcon("digital");
        customCat.setSortNo(50);

        mockMvc.perform(post("/api/finance/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customCat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.categoryName").value("数码硬件"))
                .andExpect(jsonPath("$.data.system").value(false));

        // 4. 普通用户查询可用分类：应同时看到系统分类和自定义分类
        mockMvc.perform(get("/api/finance/categories")
                        .param("categoryType", "EXPENSE")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].categoryName", hasItems("餐饮美食", "数码硬件")));
    }

    @Test
    @DisplayName("测试收支记录增删改查、BigDecimal 精度与软删除后数据过滤")
    void testFinanceRecordCrudAndPrecision() throws Exception {
        String token = registerAndGetToken("finance_user_crud", "Pass123456");

        // 预设一个收入分类
        FinanceCategory cat = new FinanceCategory(null, "月度薪资", "INCOME", "salary", 1, 1);
        financeCategoryMapper.insert(cat);

        // 1. 新增收入记录：金额 15888.88
        CreateFinanceRecordRequest req = new CreateFinanceRecordRequest();
        req.setRecordType("INCOME");
        req.setAmount(new BigDecimal("15888.88"));
        req.setCategoryId(cat.getId());
        req.setRecordDate(LocalDate.now());
        req.setTitle("2026年10月份工资");
        req.setRemark("含绩效奖金与全勤奖");

        MvcResult createRes = mockMvc.perform(post("/api/finance/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.amount").value(15888.88))
                .andExpect(jsonPath("$.data.recordType").value("INCOME"))
                .andExpect(jsonPath("$.data.categoryName").value("月度薪资"))
                .andReturn();

        long recordId = objectMapper.readTree(createRes.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2. 编辑修改金额为 16000.00
        UpdateFinanceRecordRequest updateReq = new UpdateFinanceRecordRequest();
        updateReq.setRecordType("INCOME");
        updateReq.setAmount(new BigDecimal("16000.00"));
        updateReq.setCategoryId(cat.getId());
        updateReq.setRecordDate(LocalDate.now());
        updateReq.setTitle("2026年10月份工资（重算后）");

        mockMvc.perform(put("/api/finance/records/" + recordId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.amount").value(16000.00))
                .andExpect(jsonPath("$.data.title").value("2026年10月份工资（重算后）"));

        // 3. 软删除记录
        mockMvc.perform(delete("/api/finance/records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 4. 查询详情应返回 404
        mockMvc.perform(get("/api/finance/records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));

        // 5. 列表查询应为空
        mockMvc.perform(get("/api/finance/records")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("测试业务校验：金额<=0拒绝、收支类型与分类不匹配拒绝、停用分类拒绝")
    void testAmountAndCategoryValidationRules() throws Exception {
        String token = registerAndGetToken("finance_user_val", "Pass123456");

        // 准备一个收入分类和一个停用的支出分类
        FinanceCategory incomeCat = new FinanceCategory(null, "兼职收入", "INCOME", "part_time", 1, 1);
        financeCategoryMapper.insert(incomeCat);

        FinanceCategory disabledExpenseCat = new FinanceCategory(null, "已停用分类", "EXPENSE", "none", 2, 0);
        financeCategoryMapper.insert(disabledExpenseCat);

        // 1. 金额必须大于 0：传入 0.00 应被校验拦截
        CreateFinanceRecordRequest zeroReq = new CreateFinanceRecordRequest();
        zeroReq.setRecordType("INCOME");
        zeroReq.setAmount(BigDecimal.ZERO);
        zeroReq.setRecordDate(LocalDate.now());

        mockMvc.perform(post("/api/finance/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        // 2. 负数金额被拦截
        CreateFinanceRecordRequest negReq = new CreateFinanceRecordRequest();
        negReq.setRecordType("EXPENSE");
        negReq.setAmount(new BigDecimal("-100.00"));
        negReq.setRecordDate(LocalDate.now());

        mockMvc.perform(post("/api/finance/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(negReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        // 3. 收支类型与分类类型不匹配：支出记录选用收入分类
        CreateFinanceRecordRequest mismatchReq = new CreateFinanceRecordRequest();
        mismatchReq.setRecordType("EXPENSE"); // 支出
        mismatchReq.setAmount(new BigDecimal("88.00"));
        mismatchReq.setCategoryId(incomeCat.getId()); // 收入分类
        mismatchReq.setRecordDate(LocalDate.now());

        mockMvc.perform(post("/api/finance/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mismatchReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)))
                .andExpect(jsonPath("$.message").value(containsString("分类类型不匹配")));

        // 4. 使用已停用分类被拦截
        CreateFinanceRecordRequest disabledCatReq = new CreateFinanceRecordRequest();
        disabledCatReq.setRecordType("EXPENSE");
        disabledCatReq.setAmount(new BigDecimal("50.00"));
        disabledCatReq.setCategoryId(disabledExpenseCat.getId());
        disabledCatReq.setRecordDate(LocalDate.now());

        mockMvc.perform(post("/api/finance/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disabledCatReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)))
                .andExpect(jsonPath("$.message").value(containsString("停用")));
    }

    @Test
    @DisplayName("测试月度汇总与趋势统计：收入、支出、结余精度以及分类占比")
    void testMonthlySummaryAndTrendCalculation() throws Exception {
        String token = registerAndGetToken("finance_user_stats", "Pass123456");

        FinanceCategory salaryCat = new FinanceCategory(null, "工资薪金", "INCOME", "salary", 1, 1);
        financeCategoryMapper.insert(salaryCat);

        FinanceCategory foodCat = new FinanceCategory(null, "餐饮美食", "EXPENSE", "food", 2, 1);
        financeCategoryMapper.insert(foodCat);

        FinanceCategory shoppingCat = new FinanceCategory(null, "购物消费", "EXPENSE", "shopping", 3, 1);
        financeCategoryMapper.insert(shoppingCat);

        LocalDate today = LocalDate.now();

        // 收入：10000.00
        CreateFinanceRecordRequest r1 = new CreateFinanceRecordRequest();
        r1.setRecordType("INCOME");
        r1.setAmount(new BigDecimal("10000.00"));
        r1.setCategoryId(salaryCat.getId());
        r1.setRecordDate(today);
        mockMvc.perform(post("/api/finance/records").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r1))).andExpect(status().isOk());

        // 支出 1：餐饮 3000.00
        CreateFinanceRecordRequest r2 = new CreateFinanceRecordRequest();
        r2.setRecordType("EXPENSE");
        r2.setAmount(new BigDecimal("3000.00"));
        r2.setCategoryId(foodCat.getId());
        r2.setRecordDate(today);
        mockMvc.perform(post("/api/finance/records").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r2))).andExpect(status().isOk());

        // 支出 2：购物 1000.00
        CreateFinanceRecordRequest r3 = new CreateFinanceRecordRequest();
        r3.setRecordType("EXPENSE");
        r3.setAmount(new BigDecimal("1000.00"));
        r3.setCategoryId(shoppingCat.getId());
        r3.setRecordDate(today);
        mockMvc.perform(post("/api/finance/records").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(r3))).andExpect(status().isOk());

        // 查询月度汇总：收入 10000.00，支出 4000.00，结余 6000.00
        mockMvc.perform(get("/api/finance/summary")
                        .param("year", String.valueOf(today.getYear()))
                        .param("month", String.valueOf(today.getMonthValue()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalIncome").value(10000.00))
                .andExpect(jsonPath("$.data.totalExpense").value(4000.00))
                .andExpect(jsonPath("$.data.balance").value(6000.00))
                .andExpect(jsonPath("$.data.recordCount").value(3))
                .andExpect(jsonPath("$.data.expenseCategoryStats", hasSize(2)))
                // 餐饮 3000 占 4000 的 75.0%
                .andExpect(jsonPath("$.data.expenseCategoryStats[0].categoryName").value("餐饮美食"))
                .andExpect(jsonPath("$.data.expenseCategoryStats[0].totalAmount").value(3000.00))
                .andExpect(jsonPath("$.data.expenseCategoryStats[0].percentage").value(75.0))
                // 购物 1000 占 4000 的 25.0%
                .andExpect(jsonPath("$.data.expenseCategoryStats[1].categoryName").value("购物消费"))
                .andExpect(jsonPath("$.data.expenseCategoryStats[1].totalAmount").value(1000.00))
                .andExpect(jsonPath("$.data.expenseCategoryStats[1].percentage").value(25.0));

        // 查询趋势接口
        mockMvc.perform(get("/api/finance/trends")
                        .param("year", String.valueOf(today.getYear()))
                        .param("months", "6")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalPeriodIncome").value(10000.00))
                .andExpect(jsonPath("$.data.totalPeriodExpense").value(4000.00))
                .andExpect(jsonPath("$.data.totalPeriodBalance").value(6000.00));
    }

    @Test
    @DisplayName("测试跨用户数据隔离：无法读取、修改、删除其他用户的收支明细与私有分类")
    void testCrossUserDataIsolation() throws Exception {
        String tokenA = registerAndGetToken("finance_user_A", "Pass123456");
        String tokenB = registerAndGetToken("finance_user_B", "Pass123456");

        // 用户 A 创建私有分类
        CreateFinanceCategoryRequest catReq = new CreateFinanceCategoryRequest();
        catReq.setCategoryName("A的私房钱");
        catReq.setCategoryType("INCOME");
        MvcResult catRes = mockMvc.perform(post("/api/finance/categories")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(catReq)))
                .andExpect(status().isOk())
                .andReturn();
        long catIdA = objectMapper.readTree(catRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 用户 A 创建收支记录
        CreateFinanceRecordRequest recReq = new CreateFinanceRecordRequest();
        recReq.setRecordType("INCOME");
        recReq.setAmount(new BigDecimal("5000.00"));
        recReq.setCategoryId(catIdA);
        recReq.setRecordDate(LocalDate.now());
        recReq.setTitle("秘密资金");

        MvcResult recRes = mockMvc.perform(post("/api/finance/records")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recReq)))
                .andExpect(status().isOk())
                .andReturn();
        long recIdA = objectMapper.readTree(recRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 用户 B 试图访问记录 A -> 404
        mockMvc.perform(get("/api/finance/records/" + recIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));

        // 用户 B 试图修改记录 A -> 404
        UpdateFinanceRecordRequest modReq = new UpdateFinanceRecordRequest();
        modReq.setRecordType("INCOME");
        modReq.setAmount(new BigDecimal("10.00"));
        modReq.setRecordDate(LocalDate.now());
        mockMvc.perform(put("/api/finance/records/" + recIdA)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));

        // 用户 B 试图删除记录 A -> 404
        mockMvc.perform(delete("/api/finance/records/" + recIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)));

        // 用户 B 试图使用用户 A 的私有分类记账 -> 403 / 拒绝
        CreateFinanceRecordRequest illegalReq = new CreateFinanceRecordRequest();
        illegalReq.setRecordType("INCOME");
        illegalReq.setAmount(new BigDecimal("100.00"));
        illegalReq.setCategoryId(catIdA);
        illegalReq.setRecordDate(LocalDate.now());

        mockMvc.perform(post("/api/finance/records")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(illegalReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(0)))
                .andExpect(jsonPath("$.message").value(containsString("无权使用")));
    }
}
