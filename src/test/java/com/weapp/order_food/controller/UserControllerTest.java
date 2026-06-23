package com.weapp.order_food.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weapp.order_food.model.dto.WeChatCodeDTO;
import com.weapp.order_food.service.UserService;
import com.weapp.order_food.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("用户控制器测试 - API接口验证")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private WeChatCodeDTO validWeChatCodeDTO;

    @BeforeEach
    void setUp() {
        validWeChatCodeDTO = new WeChatCodeDTO();
        validWeChatCodeDTO.setCode("valid_wechat_code_123");
    }

    // ==================== 微信登录接口测试 ====================

    @Test
    @DisplayName("测试1: 微信登录成功")
    void testLoginWithWeChat_Success() throws Exception {
        Result<String> mockResult = Result.success("登录成功", "mock_jwt_token_xyz789");

        when(userService.loginWithWeChat(anyString())).thenReturn(mockResult);

        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validWeChatCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data").value("mock_jwt_token_xyz789"));

        verify(userService, times(1)).loginWithWeChat("valid_wechat_code_123");
    }

    @Test
    @DisplayName("测试2: 微信登录失败 - 授权错误")
    void testLoginWithWeChat_AuthorizationFailure() throws Exception {
        Result<String> mockResult = Result.error("微信授权失败");

        when(userService.loginWithWeChat(anyString())).thenReturn(mockResult);

        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validWeChatCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("微信授权失败"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(userService, times(1)).loginWithWeChat("valid_wechat_code_123");
    }

    @Test
    @DisplayName("测试3: 微信登录失败 - 请求体为空对象")
    void testLoginWithWeChat_EmptyBody() throws Exception {
        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginWithWeChat(anyString());
    }

    @Test
    @DisplayName("测试4: 微信登录失败 - code字段为空字符串")
    void testLoginWithWeChat_EmptyCode() throws Exception {
        WeChatCodeDTO emptyCodeDTO = new WeChatCodeDTO();
        emptyCodeDTO.setCode("");

        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyCodeDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginWithWeChat(anyString());
    }

    @Test
    @DisplayName("测试5: 微信登录失败 - 缺少code字段")
    void testLoginWithWeChat_MissingCodeField() throws Exception {
        String jsonWithoutCode = "{\"otherField\":\"value\"}";

        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithoutCode))
                .andExpect(status().isBadRequest());

        verify(userService, never()).loginWithWeChat(anyString());
    }

    // ==================== 用户角色更新接口测试 ====================

    @Test
    @DisplayName("测试6: 更新用户角色成功")
    void testUpdateUserRole_Success() throws Exception {
        Long userId = 1001L;
        Integer role = 1;
        Result<String> mockResult = Result.success("用户身份选择成功");

        when(userService.updateUserRole(anyLong(), anyInt())).thenReturn(mockResult);

        mockMvc.perform(post("/users/role/update")
                .contentType(MediaType.APPLICATION_JSON)
                .param("userId", userId.toString())
                .param("role", role.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("用户身份选择成功"));

        verify(userService, times(1)).updateUserRole(userId, role);
    }

    @Test
    @DisplayName("测试7: 更新用户角色失败 - 用户不存在")
    void testUpdateUserRole_UserNotFound() throws Exception {
        Long userId = 9999L;
        Integer role = 1;
        Result<String> mockResult = Result.error("用户不存在");

        when(userService.updateUserRole(anyLong(), anyInt())).thenReturn(mockResult);

        mockMvc.perform(post("/users/role/update")
                .contentType(MediaType.APPLICATION_JSON)
                .param("userId", userId.toString())
                .param("role", role.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(userService, times(1)).updateUserRole(userId, role);
    }

    @Test
    @DisplayName("测试8: 更新用户角色失败 - 参数缺失")
    void testUpdateUserRole_MissingParameters() throws Exception {
        mockMvc.perform(post("/users/role/update")
                .contentType(MediaType.APPLICATION_JSON)
                .param("userId", "1001"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserRole(anyLong(), anyInt());
    }
}

