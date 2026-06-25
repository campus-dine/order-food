package com.weapp.order_food.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HttpClientUtil 工具类测试")
class HttpClientUtilTest {

    @Test
    @DisplayName("测试1: buildRequestConfig返回非空配置")
    void testBuildRequestConfig_NotNull() {
        org.apache.http.client.config.RequestConfig config = HttpClientUtil.buildRequestConfig();

        assertNotNull(config);
        assertEquals(5000, config.getConnectTimeout());
        assertEquals(5000, config.getConnectionRequestTimeout());
        assertEquals(5000, config.getSocketTimeout());
    }

    @Test
    @DisplayName("测试2: doGet请求 - 无效URL返回空字符串")
    void testDoGet_InvalidUrl_ReturnsEmptyString() {
        String result = HttpClientUtil.doGet("http://invalid.url.that.does.not.exist", null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("测试3: doGet请求 - null参数不报错")
    void testDoGet_NullParams_NoError() {
        String result = HttpClientUtil.doGet("http://www.baidu.com", null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试4: doGet请求 - 带参数")
    void testDoGet_WithParams() {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        params.put("key2", "value2");

        String result = HttpClientUtil.doGet("http://www.baidu.com", params);

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试5: doPost请求 - 无效URL返回空字符串")
    void testDoPost_InvalidUrl_ReturnsEmptyString() {
        String result = HttpClientUtil.doPost("http://invalid.url.that.does.not.exist", null);

        assertEquals("", result);
    }

    @Test
    @DisplayName("测试6: doPost请求 - null参数不报错")
    void testDoPost_NullParams_NoError() {
        String result = HttpClientUtil.doPost("http://www.baidu.com", null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试7: doPost请求 - 带参数")
    void testDoPost_WithParams() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "testuser");
        params.put("password", "testpass");

        String result = HttpClientUtil.doPost("http://www.baidu.com", params);

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试8: doGet请求 - 空Map参数")
    void testDoGet_EmptyMap() {
        Map<String, String> emptyMap = new HashMap<>();

        String result = HttpClientUtil.doGet("http://www.baidu.com", emptyMap);

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试9: doPost请求 - 空Map参数")
    void testDoPost_EmptyMap() {
        Map<String, String> emptyMap = new HashMap<>();

        String result = HttpClientUtil.doPost("http://www.baidu.com", emptyMap);

        assertNotNull(result);
    }

    @Test
    @DisplayName("测试10: 超时配置正确")
    void testTimeoutConfiguration() {
        org.apache.http.client.config.RequestConfig config = HttpClientUtil.buildRequestConfig();

        assertEquals(5000, config.getConnectTimeout(), "连接超时应为5000ms");
        assertEquals(5000, config.getConnectionRequestTimeout(), "连接请求超时应为5000ms");
        assertEquals(5000, config.getSocketTimeout(), "Socket超时应为5000ms");
    }
}
