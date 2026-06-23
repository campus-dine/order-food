package com.weapp.order_food.service;

import com.weapp.order_food.config.WeChatProperties;
import com.weapp.order_food.entity.User;
import com.weapp.order_food.mapper.UserMapper;
import com.weapp.order_food.model.dto.WeChatCodeDTO;
import com.weapp.order_food.service.impl.UserServiceImpl;
import com.weapp.order_food.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("用户服务测试 - 微信登录与注册")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private WeChatProperties weChatProperties;

    @SpyBean
    private UserServiceImpl userServiceImpl;

    private WeChatCodeDTO testWeChatCodeDTO;

    @BeforeEach
    void setUp() {
        testWeChatCodeDTO = new WeChatCodeDTO();
        testWeChatCodeDTO.setCode("test_code_12345");
        
        when(weChatProperties.getAppId()).thenReturn("test_appid");
        when(weChatProperties.getSecret()).thenReturn("test_secret");
    }

    @Test
    @DisplayName("测试1: 老用户微信登录成功")
    void testLoginWithWeChat_ExistingUser_Success() throws Exception {
        String testOpenId = "test_openid_existing";
        Long testUserId = 1001L;
        String testCode = "valid_code";

        doReturn(testOpenId).when(userServiceImpl).getOpenId(anyString());
        when(userMapper.getUserByOpenId(testOpenId)).thenReturn(testUserId);

        Result<String> result = userService.loginWithWeChat(testCode);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("登录成功", result.getMessage());
        assertNotNull(result.getData());
        assertTrue(result.getData().length() > 0);

        verify(userServiceImpl, times(1)).getOpenId(testCode);
        verify(userMapper, times(1)).getUserByOpenId(testOpenId);
        verify(userMapper, never()).insertUsers(any(User.class));
    }

    @Test
    @DisplayName("测试2: 新用户微信登录自动注册")
    void testLoginWithWeChat_NewUser_AutoRegister() throws Exception {
        String testOpenId = "test_openid_new";
        String testCode = "new_user_code";

        doReturn(testOpenId).when(userServiceImpl).getOpenId(anyString());
        when(userMapper.getUserByOpenId(testOpenId)).thenReturn(null);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(9999L);
            return null;
        }).when(userMapper).insertUsers(any(User.class));

        Result<String> result = userService.loginWithWeChat(testCode);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("登录成功", result.getMessage());
        assertNotNull(result.getData());

        verify(userServiceImpl, times(1)).getOpenId(testCode);
        verify(userMapper, times(1)).getUserByOpenId(testOpenId);
        verify(userMapper, times(1)).insertUsers(any(User.class));
    }

    @Test
    @DisplayName("测试3: 微信登录失败 - 无效code")
    void testLoginWithWeChat_InvalidCode_Failure() throws Exception {
        String invalidCode = "invalid_code";

        doReturn(null).when(userServiceImpl).getOpenId(anyString());

        Result<String> result = userService.loginWithWeChat(invalidCode);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("微信授权失败", result.getMessage());
        assertNull(result.getData());

        verify(userServiceImpl, times(1)).getOpenId(invalidCode);
        verify(userMapper, never()).getUserByOpenId(anyString());
        verify(userMapper, never()).insertUsers(any(User.class));
    }

    @Test
    @DisplayName("测试4: DTO数据验证")
    void testWeChatCodeDTO() {
        WeChatCodeDTO dto = new WeChatCodeDTO();
        dto.setCode("test_code");

        assertEquals("test_code", dto.getCode());
    }
}
