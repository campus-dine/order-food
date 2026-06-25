package com.weapp.order_food.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result 工具类测试")
class ResultTest {

    @Test
    @DisplayName("测试1: success单参数 - data字段正确设置")
    void testSuccess_SingleParameter() {
        Result<String> result = Result.success("test_data");

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test_data", result.getData());
    }

    @Test
    @DisplayName("测试2: success双参数 - message和data正确设置")
    void testSuccess_TwoParameters() {
        Result<String> result = Result.success("操作成功", "test_data");

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("test_data", result.getData());
    }

    @Test
    @DisplayName("测试3: error单参数 - code为500")
    void testError_SingleParameter() {
        Result<Void> result = Result.error("服务器错误");

        assertEquals(500, result.getCode());
        assertEquals("服务器错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试4: error双参数 - 自定义错误码")
    void testError_TwoParameters() {
        Result<Void> result = Result.error(404, "资源不存在");

        assertEquals(404, result.getCode());
        assertEquals("资源不存在", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试5: 构造函数 - 单参数构造")
    void testConstructor_SingleParameter() {
        Result<Integer> result = new Result<>(100);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(100, result.getData());
    }

    @Test
    @DisplayName("测试6: 构造函数 - 三参数构造(success=true)")
    void testConstructor_ThreeParameters_Success() {
        Result<String> result = new Result<>("data", true, null);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("测试7: 构造函数 - 三参数构造(success=false)")
    void testConstructor_ThreeParameters_Failure() {
        Result<String> result = new Result<>(null, false, "自定义错误");

        assertEquals(500, result.getCode());
        assertEquals("自定义错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试8: 构造函数 - 两参数构造")
    void testConstructor_TwoParameters() {
        Result<Void> result = new Result<>(403, "禁止访问");

        assertEquals(403, result.getCode());
        assertEquals("禁止访问", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试9: 无参构造函数")
    void testConstructor_NoParameters() {
        Result<Void> result = new Result<>();

        assertEquals(0, result.getCode());
        assertNull(result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("测试10: 泛型支持 - List类型")
    void testGeneric_ListType() {
        java.util.List<String> list = java.util.Arrays.asList("item1", "item2");
        Result<java.util.List<String>> result = Result.success(list);

        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
        assertEquals("item1", result.getData().get(0));
    }
}
