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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("用户控制器测试 - 微信登录接口")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private WeChatCodeDTO testWeChatCodeDTO;

    @BeforeEach
    void setUp() {
        testWeChatCodeDTO = new WeChatCodeDTO();
        testWeChatCodeDTO.setCode("test_wechat_code");
    }

    @Test
    @DisplayName("测试微信登录接口 - 成功")
    void testLoginWithWeChat_Success() throws Exception {
        Result<String> mockResult = Result.success("登录成功", "mock_jwt_token_12345");

        when(userService.loginWithWeChat(anyString())).thenReturn(mockResult);

        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testWeChatCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data").value("mock_jwt_token_12345"));

        verify(userService, times(1)).loginWithWeChat("test_wechat_code");
    }

    @Test
    @DisplayName("测试微信登录接口 - 失败")
    void testLoginWithWeChat_Failure() throws Exception {
        Result<String> mockResult = Result.error("微信授权失败");

        when(userService.loginWithWeChat(anyString())).thenReturn(mockResult);

        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testWeChatCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("微信授权失败"));

        verify(userService, times(1)).loginWithWeChat("test_wechat_code");
    }

    @Test
    @DisplayName("测试微信登录接口 - 请求体为空")
    void testLoginWithWeChat_EmptyBody() throws Exception {
        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

