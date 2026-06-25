package com.weapp.order_food.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("LoginInterceptor 拦截器测试")
class LoginInterceptorTest {

    private static LoginInterceptor interceptor;

    @BeforeAll
    static void setUp() {
        ReflectionTestUtils.setField(JwtTokenUtil.class, "javaSecretKey",
            io.jsonwebtoken.security.Keys.hmacShaKeyFor("mySecretKeyForJwtTesting123456789".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        ReflectionTestUtils.setField(JwtTokenUtil.class, "expirationTime", 3600000L);

        interceptor = new LoginInterceptor();
    }

    @Test
    @DisplayName("测试1: 登录请求放行")
    void testPreHandle_LoginRequest_AllowPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/login/wechat");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result, "登录请求应该被放行");
        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("测试2: OPTIONS请求放行")
    void testPreHandle_OptionsRequest_AllowPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/some-endpoint");
        request.setMethod("OPTIONS");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result, "OPTIONS请求应该被放行");
    }

    @Test
    @DisplayName("测试3: 缺少Token返回401")
    void testPreHandle_MissingToken_Return401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/customer/order/list");
        request.setMethod("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result, "缺少Token应该被拦截");
        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("测试4: 空Token返回401")
    void testPreHandle_EmptyToken_Return401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/customer/order/list");
        request.setMethod("GET");
        request.addHeader("Authorization", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result, "空Token应该被拦截");
        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("测试5: 有效Token放行")
    void testPreHandle_ValidToken_AllowPass() throws Exception {
        try (MockedStatic<JwtTokenUtil> mockedJwt = mockStatic(JwtTokenUtil.class)) {
            mockedJwt.when(() -> JwtTokenUtil.parseTokenGetUserId(anyString()))
                    .thenReturn("1001");

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/customer/order/list");
            request.setMethod("GET");
            request.addHeader("Authorization", "Bearer valid_token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, null);

            assertTrue(result, "有效Token应该被放行");
            assertEquals("1001", request.getAttribute("userId"));
        }
    }

    @Test
    @DisplayName("测试6: 无效Token返回401")
    void testPreHandle_InvalidToken_Return401() throws Exception {
        try (MockedStatic<JwtTokenUtil> mockedJwt = mockStatic(JwtTokenUtil.class)) {
            mockedJwt.when(() -> JwtTokenUtil.parseTokenGetUserId(anyString()))
                    .thenThrow(new RuntimeException("Token解析失败"));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/customer/order/list");
            request.setMethod("GET");
            request.addHeader("Authorization", "Bearer invalid_token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, null);

            assertFalse(result, "无效Token应该被拦截");
            assertEquals(401, response.getStatus());
        }
    }

    @Test
    @DisplayName("测试7: URL包含login关键字放行")
    void testPreHandle_UrlContainsLogin_AllowPass() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/login/wechat");
        request.setMethod("POST");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result, "包含login的URL应该被放行");
    }

    @Test
    @DisplayName("测试8: Token解析后userId设置到request")
    void testPreHandle_TokenParsed_SetUserIdToRequest() throws Exception {
        try (MockedStatic<JwtTokenUtil> mockedJwt = mockStatic(JwtTokenUtil.class)) {
            mockedJwt.when(() -> JwtTokenUtil.parseTokenGetUserId(anyString()))
                    .thenReturn("9999");

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/merchant/order/list");
            request.setMethod("GET");
            request.addHeader("Authorization", "Bearer some_token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, null);

            assertTrue(result);
            assertEquals("9999", request.getAttribute("userId"));
        }
    }
}
