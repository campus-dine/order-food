package com.weapp.order_food.service;

import com.weapp.order_food.config.WeChatProperties;
import com.weapp.order_food.entity.User;
import com.weapp.order_food.mapper.UserMapper;
import com.weapp.order_food.service.impl.UserServiceImpl;
import com.weapp.order_food.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("用户服务测试 - 微信登录与角色管理")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private WeChatProperties weChatProperties;

    @SpyBean
    private UserServiceImpl userServiceImpl;

    @BeforeEach
    void setUp() {
        when(weChatProperties.getAppId()).thenReturn("test_appid");
        when(weChatProperties.getSecret()).thenReturn("test_secret");
    }

    // ==================== 微信登录测试 ====================

    @Test
    @DisplayName("测试1: 老用户微信登录成功")
    void testLoginWithWeChat_ExistingUser_Success() throws Exception {
        String testOpenId = "test_openid_existing";
        Long testUserId = 1001L;
        String testCode = "valid_code";
        User existingUser = User.builder()
                .id(testUserId)
                .openId(testOpenId)
                .role("1")
                .build();

        doReturn(testOpenId).when(userServiceImpl).getOpenId(anyString());
        when(userMapper.getUserByOpenId(testOpenId)).thenReturn(testUserId);
        when(userMapper.selectById(testUserId)).thenReturn(existingUser);

        Result<String> result = userService.loginWithWeChat(testCode);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("登录成功", result.getMessage());
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());

        verify(userServiceImpl, times(1)).getOpenId(testCode);
        verify(userMapper, times(1)).getUserByOpenId(testOpenId);
        verify(userMapper, times(1)).selectById(testUserId);
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

    // ==================== 用户角色更新测试 ====================

    @Test
    @DisplayName("测试4: 更新用户角色成功")
    void testUpdateUserRole_Success() {
        Long userId = 1001L;
        Integer role = 1;
        User existingUser = User.builder()
                .id(userId)
                .openId("test_openid")
                .role("0")
                .createTime(LocalDateTime.now())
                .build();

        when(userMapper.selectById(userId)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        Result<String> result = userService.updateUserRole(userId, role);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("用户身份选择成功", result.getData());

        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试5: 更新用户角色失败 - 用户不存在")
    void testUpdateUserRole_UserNotFound() {
        Long userId = 9999L;
        Integer role = 1;

        when(userMapper.selectById(userId)).thenReturn(null);

        Result<String> result = userService.updateUserRole(userId, role);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("用户不存在", result.getMessage());

        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试6: 更新用户角色失败 - 数据库更新失败")
    void testUpdateUserRole_UpdateFailed() {
        Long userId = 1001L;
        Integer role = 1;
        User existingUser = User.builder()
                .id(userId)
                .openId("test_openid")
                .role("0")
                .build();

        when(userMapper.selectById(userId)).thenReturn(existingUser);
        when(userMapper.updateById(any(User.class))).thenReturn(0);

        Result<String> result = userService.updateUserRole(userId, role);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("用户身份选择失败", result.getMessage());

        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("测试7: 更新用户角色 - 验证角色类型转换")
    void testUpdateUserRole_RoleTypeConversion() {
        Long userId = 1001L;
        Integer role = 2;
        User existingUser = User.builder()
                .id(userId)
                .openId("test_openid")
                .role("0")
                .build();

        when(userMapper.selectById(userId)).thenReturn(existingUser);
        when(userMapper.updateById(argThat(user -> 
            user != null && "2".equals(user.getRole())
        ))).thenReturn(1);

        Result<String> result = userService.updateUserRole(userId, role);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("用户身份选择成功", result.getData());
    }
}
