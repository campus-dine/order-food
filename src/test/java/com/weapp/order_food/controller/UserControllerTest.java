package com.weapp.order_food.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weapp.order_food.model.dto.WeChatCodeDTO;
import com.weapp.order_food.service.RoleService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("用户控制器测试 - API接口验证")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleService roleService;

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

    @Test
    @DisplayName("测试6: 微信登录失败 - Service抛出异常")
    void testLoginWithWeChat_ServiceException() throws Exception {
        when(userService.loginWithWeChat(anyString()))
                .thenThrow(new RuntimeException("网络请求失败"));

        mockMvc.perform(post("/users/login/wechat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validWeChatCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("网络请求失败"));

        verify(userService, times(1)).loginWithWeChat("valid_wechat_code_123");
    }

    // ==================== 选择/切换用户身份接口测试 ====================

    @Test
    @DisplayName("测试7: 选择用户角色成功 - 客户角色")
    void testChooseRole_Customer_Success() throws Exception {
        Long userId = 1001L;
        Integer role = 0;
        String mockToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock";
        String newToken = "new_jwt_token_customer";

        Result<String> roleResult = Result.success("身份选择成功", newToken);

        when(roleService.chooseUserRole(eq(userId), eq(role))).thenReturn(roleResult);

        mockMvc.perform(put("/users/role")
                .header("Authorization", mockToken)
                .param("role", role.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value(newToken));

        verify(roleService, times(1)).chooseUserRole(userId, role);
    }

    @Test
    @DisplayName("测试8: 选择用户角色成功 - 商家角色")
    void testChooseRole_Merchant_Success() throws Exception {
        Long userId = 1001L;
        Integer role = 1;
        String mockToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock";
        String newToken = "new_jwt_token_merchant";

        Result<String> roleResult = Result.success("身份选择成功", newToken);

        when(roleService.chooseUserRole(eq(userId), eq(role))).thenReturn(roleResult);

        mockMvc.perform(put("/users/role")
                .header("Authorization", mockToken)
                .param("role", role.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value(newToken));

        verify(roleService, times(1)).chooseUserRole(userId, role);
    }

    @Test
    @DisplayName("测试9: 选择用户角色失败 - 角色参数非法")
    void testChooseRole_InvalidRole_Failure() throws Exception {
        String mockToken = "Bearer mock_jwt_token";

        mockMvc.perform(put("/users/role")
                .header("Authorization", mockToken)
                .param("role", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("身份参数不正确"));

        verify(roleService, never()).chooseUserRole(anyLong(), anyInt());
    }

    @Test
    @DisplayName("测试10: 选择用户角色失败 - 缺少role参数")
    void testChooseRole_MissingRole_Failure() throws Exception {
        String mockToken = "Bearer mock_jwt_token";

        mockMvc.perform(put("/users/role")
                .header("Authorization", mockToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(roleService, never()).chooseUserRole(anyLong(), anyInt());
    }

    @Test
    @DisplayName("测试11: 选择用户角色失败 - 缺少Authorization头")
    void testChooseRole_MissingToken_Failure() throws Exception {
        mockMvc.perform(put("/users/role")
                .param("role", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(roleService, never()).chooseUserRole(anyLong(), anyInt());
    }

    @Test
    @DisplayName("测试12: 选择用户角色失败 - roleService返回失败")
    void testChooseRole_RoleServiceFailure() throws Exception {
        Long userId = 1001L;
        Integer role = 1;
        String mockToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock";

        Result<String> roleResult = Result.error("身份选择失败");

        when(roleService.chooseUserRole(eq(userId), eq(role))).thenReturn(roleResult);

        mockMvc.perform(put("/users/role")
                .header("Authorization", mockToken)
                .param("role", role.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("身份选择失败"));

        verify(roleService, times(1)).chooseUserRole(userId, role);
    }
}
