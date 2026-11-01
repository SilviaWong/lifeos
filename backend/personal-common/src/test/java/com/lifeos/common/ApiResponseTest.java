package com.lifeos.common;

import com.lifeos.common.api.ApiResponse;
import com.lifeos.common.api.CommonErrorCode;
import com.lifeos.common.api.PageQuery;
import com.lifeos.common.api.PageResult;
import com.lifeos.common.util.TraceIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiResponse 与分页对象测试")
class ApiResponseTest {

    @AfterEach
    void tearDown() {
        TraceIdUtil.clear();
    }

    @Test
    @DisplayName("测试成功响应结构")
    void testSuccessResponse() {
        TraceIdUtil.initTraceId("test-trace-id-12345");
        ApiResponse<String> response = ApiResponse.success("hello world");

        assertEquals(0, response.getCode());
        assertEquals("操作成功", response.getMessage());
        assertEquals("hello world", response.getData());
        assertEquals("test-trace-id-12345", response.getTraceId());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    @DisplayName("测试失败响应结构")
    void testFailResponse() {
        ApiResponse<Void> response = ApiResponse.fail(CommonErrorCode.BAD_REQUEST, "自定义参数错误");

        assertEquals(400, response.getCode());
        assertEquals("自定义参数错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("测试分页查询参数边界保护")
    void testPageQueryBoundary() {
        PageQuery query = new PageQuery(0, 500);

        assertEquals(1, query.getPageNum(), "非法页码自动兜底为 1");
        assertEquals(100, query.getPageSize(), "超限每页大小自动限制为 100");
        assertEquals(0L, query.getOffset(), "第 1 页 offset 应为 0");

        query.setPageNum(3);
        query.setPageSize(20);
        assertEquals(40L, query.getOffset(), "第 3 页 每页 20 条 offset 应为 40");
    }

    @Test
    @DisplayName("测试分页结果计算总页数")
    void testPageResult() {
        List<String> items = List.of("A", "B", "C");
        PageResult<String> result = PageResult.of(items, 45, 1, 10);

        assertEquals(3, result.getRecords().size());
        assertEquals(45, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(5, result.getTotalPages(), "45 条数据每页 10 条共 5 页");
    }
}

