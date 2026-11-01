package com.lifeos.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.business.dto.*;
import com.lifeos.business.entity.Diary;
import com.lifeos.business.entity.ItemLending;
import com.lifeos.business.entity.Memo;
import com.lifeos.business.entity.PersonalItem;
import com.lifeos.business.mapper.DiaryMapper;
import com.lifeos.business.mapper.ItemLendingMapper;
import com.lifeos.business.mapper.MemoMapper;
import com.lifeos.business.mapper.PersonalItemMapper;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("生活记录：个人物品与借还管理、备忘录、心情日记及用户隐私数据隔离集成测试")
class LifeRecordManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonalItemMapper personalItemMapper;

    @Autowired
    private ItemLendingMapper itemLendingMapper;

    @Autowired
    private MemoMapper memoMapper;

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @BeforeEach
    void setUp() {
        itemLendingMapper.delete(null);
        personalItemMapper.delete(null);
        memoMapper.delete(null);
        diaryMapper.delete(null);
    }

    private String registerAndGetToken(String username, String password) throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername(username);
        registerDTO.setPassword(password);
        registerDTO.setPhone("137" + System.currentTimeMillis() % 100000000);
        registerDTO.setNickname("记录人_" + username);

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

    @Test
    @DisplayName("测试物品管理：创建、查询、编辑、归档与删除完整闭环")
    void testItemCrudAndArchive() throws Exception {
        String token = registerAndGetToken("item_user_1", "Pass123456");

        // 1. 新增物品：Switch 游戏机
        CreateItemRequest req = new CreateItemRequest();
        req.setItemName("Switch OLED 游戏机");
        req.setCategory("电子数码");
        req.setQuantity(new BigDecimal("1.00"));
        req.setUnit("台");
        req.setStorageLocation("客厅电视机抽屉");
        req.setPurchaseDate(LocalDate.of(2025, 6, 18));
        req.setRemark("含塞尔达传说卡带");

        MvcResult createRes = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.itemName").value("Switch OLED 游戏机"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.isLent").value(false))
                .andReturn();

        Long itemId = objectMapper.readTree(createRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. 查看物品详情
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.item.itemName").value("Switch OLED 游戏机"))
                .andExpect(jsonPath("$.data.lendingHistory", hasSize(0)));

        // 3. 修改物品信息
        UpdateItemRequest updateReq = new UpdateItemRequest();
        updateReq.setItemName("Switch OLED 游戏机(已贴膜)");
        updateReq.setCategory("电子数码");
        updateReq.setQuantity(new BigDecimal("1.00"));
        updateReq.setUnit("台");
        updateReq.setStorageLocation("卧室书架二层");
        updateReq.setRemark("新增两副原装手柄");

        mockMvc.perform(put("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.itemName").value("Switch OLED 游戏机(已贴膜)"))
                .andExpect(jsonPath("$.data.storageLocation").value("卧室书架二层"));

        // 4. 物品归档
        mockMvc.perform(put("/api/items/" + itemId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));

        // 5. 恢复物品
        mockMvc.perform(put("/api/items/" + itemId + "/archive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // 6. 逻辑删除物品
        mockMvc.perform(delete("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 再次查看已被删除
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("测试物品借还：登记借出、重复借出拒绝、借出中删除拒绝、登记归还与完整历史追溯")
    void testItemLendingLifecycleAndConstraints() throws Exception {
        String token = registerAndGetToken("lending_user_1", "Pass123456");

        // 1. 创建物品：iPad Pro
        CreateItemRequest itemReq = new CreateItemRequest();
        itemReq.setItemName("iPad Pro 11寸");
        itemReq.setCategory("电子数码");

        MvcResult itemRes = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isOk())
                .andReturn();
        Long itemId = objectMapper.readTree(itemRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. 登记借出给张三
        CreateLendingRequest lendReq = new CreateLendingRequest();
        lendReq.setBorrowerName("同事张三");
        lendReq.setLentAt(LocalDateTime.now().minusDays(2));
        lendReq.setExpectedReturnAt(LocalDateTime.now().plusDays(5));
        lendReq.setRemark("出差做汇报演示使用");

        MvcResult lendRes = mockMvc.perform(post("/api/items/" + itemId + "/lendings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lendReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.borrowerName").value("同事张三"))
                .andExpect(jsonPath("$.data.status").value("LENT"))
                .andReturn();
        Long lendingId = objectMapper.readTree(lendRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 3. 校验物品状态变为借出中
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.item.isLent").value(true))
                .andExpect(jsonPath("$.data.item.currentBorrower").value("同事张三"))
                .andExpect(jsonPath("$.data.activeLending.id").value(lendingId));

        // 4. 关键规则约束：同一物品未归还前，拒绝重复借出
        CreateLendingRequest lendReq2 = new CreateLendingRequest();
        lendReq2.setBorrowerName("朋友李四");
        lendReq2.setLentAt(LocalDateTime.now());
        mockMvc.perform(post("/api/items/" + itemId + "/lendings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lendReq2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("不能重复借出")));

        // 5. 关键规则约束：借出中的物品不可删除
        mockMvc.perform(delete("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("不能删除，请先登记归还")));

        // 6. 登记归还
        ReturnLendingRequest returnReq = new ReturnLendingRequest();
        returnReq.setReturnedAt(LocalDateTime.now());
        returnReq.setRemark("设备完好无损，已清洁");

        mockMvc.perform(post("/api/lendings/" + lendingId + "/return")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("RETURNED"));

        // 7. 再次重复登记归还 -> 拒绝
        mockMvc.perform(post("/api/lendings/" + lendingId + "/return")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("请勿重复操作")));

        // 8. 归还后物品恢复为在库
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.item.isLent").value(false))
                .andExpect(jsonPath("$.data.activeLending").doesNotExist())
                .andExpect(jsonPath("$.data.lendingHistory", hasSize(1)))
                .andExpect(jsonPath("$.data.lendingHistory[0].status").value("RETURNED"));
    }

    @Test
    @DisplayName("测试备忘录管理：创建、置顶、取消置顶、模糊搜索与分页")
    void testMemoCrudAndPin() throws Exception {
        String token = registerAndGetToken("memo_user_1", "Pass123456");

        // 1. 创建普通备忘
        CreateMemoRequest memo1 = new CreateMemoRequest();
        memo1.setTitle("房屋租赁合同到期续签");
        memo1.setContent("记得在11月20日前联系房东确认续约租金");
        memo1.setTags("生活,租房,合同");
        memo1.setIsPinned(0);

        mockMvc.perform(post("/api/memos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memo1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isPinned").value(0));

        // 2. 创建重要置顶备忘
        CreateMemoRequest memo2 = new CreateMemoRequest();
        memo2.setTitle("紧急：服务器升级关键参数");
        memo2.setContent("MySQL 8.0 缓冲池设置为 4G，连接超时 30s");
        memo2.setTags("工作,技术,重点");
        memo2.setIsPinned(1);

        MvcResult memo2Res = mockMvc.perform(post("/api/memos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memo2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isPinned").value(1))
                .andReturn();
        Long memo2Id = objectMapper.readTree(memo2Res.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 3. 列表查询：置顶的 memo2 必须排在首位
        mockMvc.perform(get("/api/memos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value(memo2Id))
                .andExpect(jsonPath("$.data.records[0].title").value("紧急：服务器升级关键参数"));

        // 4. 关键字搜索查询
        mockMvc.perform(get("/api/memos")
                        .param("keyword", "租房")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("房屋租赁合同到期续签"));

        // 5. 取消置顶
        mockMvc.perform(put("/api/memos/" + memo2Id + "/pin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isPinned").value(0));
    }

    @Test
    @DisplayName("测试日记管理：按日期记录、同日重复创建拒绝、修改、心情天气与删除")
    void testDiaryCrudAndDateConflict() throws Exception {
        String token = registerAndGetToken("diary_user_1", "Pass123456");

        LocalDate today = LocalDate.now();

        // 1. 写今日日记
        CreateDiaryRequest diaryReq = new CreateDiaryRequest();
        diaryReq.setDiaryDate(today);
        diaryReq.setTitle("高效自律的一天");
        diaryReq.setContent("今天完成了个人生活管理系统的开发，感觉非常充实！");
        diaryReq.setMood("HAPPY");
        diaryReq.setWeather("SUNNY");

        MvcResult diaryRes = mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diaryReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.diaryDate").value(today.toString()))
                .andExpect(jsonPath("$.data.moodDesc", containsString("开心")))
                .andExpect(jsonPath("$.data.weatherDesc", containsString("晴天")))
                .andReturn();
        Long diaryId = objectMapper.readTree(diaryRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. 关键规则约束：同一天不允许重复创建日记，防止覆盖冲突
        CreateDiaryRequest conflictReq = new CreateDiaryRequest();
        conflictReq.setDiaryDate(today);
        conflictReq.setTitle("重复写日记");
        conflictReq.setContent("应该被拦截");
        mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("已经记录过日记")));

        // 3. 修改日记
        UpdateDiaryRequest updateReq = new UpdateDiaryRequest();
        updateReq.setDiaryDate(today);
        updateReq.setTitle("高效自律的一天(晚间复盘版)");
        updateReq.setContent("晚上又去跑步了5公里，出了一身汗，特别舒服！");
        updateReq.setMood("CALM");
        updateReq.setWeather("SUNNY");

        mockMvc.perform(put("/api/diaries/" + diaryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("高效自律的一天(晚间复盘版)"))
                .andExpect(jsonPath("$.data.moodDesc", containsString("平静")));

        // 4. 查询日记详情
        mockMvc.perform(get("/api/diaries/" + diaryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", containsString("晚上又去跑步了5公里")));

        // 5. 删除日记
        mockMvc.perform(delete("/api/diaries/" + diaryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 再次查询已不存在
        mockMvc.perform(get("/api/diaries/" + diaryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("测试严格数据隔离：用户不能查看、修改、借还或删除其他用户的物品、备忘录与私密日记")
    void testCrossUserDataIsolationForLifeRecords() throws Exception {
        String tokenUserA = registerAndGetToken("life_iso_user_a", "Pass123456");
        String tokenUserB = registerAndGetToken("life_iso_user_b", "Pass123456");

        // 1. 用户 A 创建物品
        CreateItemRequest itemA = new CreateItemRequest();
        itemA.setItemName("UserA 的降噪耳机");
        MvcResult itemRes = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemA)))
                .andReturn();
        Long itemIdA = objectMapper.readTree(itemRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 用户 A 登记借出
        CreateLendingRequest lendA = new CreateLendingRequest();
        lendA.setBorrowerName("小明");
        lendA.setLentAt(LocalDateTime.now());
        MvcResult lendRes = mockMvc.perform(post("/api/items/" + itemIdA + "/lendings")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lendA)))
                .andReturn();
        Long lendingIdA = objectMapper.readTree(lendRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 2. 用户 A 创建备忘录
        CreateMemoRequest memoA = new CreateMemoRequest();
        memoA.setTitle("UserA 个人银行账户信息");
        memoA.setContent("私密账户密码信息，绝不外泄");
        MvcResult memoRes = mockMvc.perform(post("/api/memos")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memoA)))
                .andReturn();
        Long memoIdA = objectMapper.readTree(memoRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 3. 用户 A 创建私密日记
        CreateDiaryRequest diaryA = new CreateDiaryRequest();
        diaryA.setDiaryDate(LocalDate.now());
        diaryA.setTitle("UserA 的私人独白");
        diaryA.setContent("非常敏感的个人生活日记");
        MvcResult diaryRes = mockMvc.perform(post("/api/diaries")
                        .header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(diaryA)))
                .andReturn();
        Long diaryIdA = objectMapper.readTree(diaryRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // 4. 用户 B 尝试越权访问 User A 的物品详情 -> 404
        mockMvc.perform(get("/api/items/" + itemIdA)
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 5. 用户 B 尝试越权归还 User A 的借出记录 -> 404
        mockMvc.perform(post("/api/lendings/" + lendingIdA + "/return")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 6. 用户 B 尝试越权查看 User A 的备忘录详情 -> 404
        mockMvc.perform(get("/api/memos/" + memoIdA)
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 7. 用户 B 尝试越权查看 User A 的私密日记 -> 404
        mockMvc.perform(get("/api/diaries/" + diaryIdA)
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 8. 用户 B 列表查询，完全无法感知 User A 的任何生活记录数据
        mockMvc.perform(get("/api/items")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(get("/api/memos")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(get("/api/diaries")
                        .header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }
}

