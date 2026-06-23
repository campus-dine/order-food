package com.weapp.order_food.service;

import com.weapp.order_food.entity.Customer;
import com.weapp.order_food.entity.Merchant;
import com.weapp.order_food.mapper.CustomerMapper;
import com.weapp.order_food.mapper.MerchantMapper;
import com.weapp.order_food.service.impl.RoleServiceImpl;
import com.weapp.order_food.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("角色服务测试 - 用户身份选择与开户")
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomerMapper customerMapper;

    @MockBean
    private MerchantMapper merchantMapper;

    @SpyBean
    private RoleServiceImpl roleServiceImpl;

    @BeforeEach
    void setUp() {
    }

    // ==================== 客户身份测试 ====================

    @Test
    @DisplayName("测试1: 选择客户身份成功-新客户开户")
    void testChooseUserRole_Customer_NewCustomer() {
        Long userId = 1001L;
        Integer role = 0;

        when(userService.updateUserRole(userId, role))
            .thenReturn(Result.success("用户身份选择成功"));
        when(customerMapper.selectCount(any())).thenReturn(0L);
        when(customerMapper.insert(any(Customer.class))).thenReturn(1);

        Result<String> result = roleService.chooseUserRole(userId, role);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("用户身份选择并初始化成功", result.getData());

        verify(userService, times(1)).updateUserRole(userId, role);
        verify(customerMapper, times(1)).selectCount(any());
        verify(customerMapper, times(1)).insert(any(Customer.class));
        verify(merchantMapper, never()).insert(any(Merchant.class));
    }

    @Test
    @DisplayName("测试2: 选择客户身份成功-已有客户账户")
    void testChooseUserRole_Customer_ExistingCustomer() {
        Long userId = 1002L;
        Integer role = 0;

        when(userService.updateUserRole(userId, role))
            .thenReturn(Result.success("用户身份选择成功"));
        when(customerMapper.selectCount(any())).thenReturn(1L);

        Result<String> result = roleService.chooseUserRole(userId, role);

        assertEquals(200, result.getCode());
        assertEquals("用户身份选择并初始化成功", result.getData());

        verify(userService, times(1)).updateUserRole(userId, role);
        verify(customerMapper, times(1)).selectCount(any());
        verify(customerMapper, never()).insert(any(Customer.class));
    }

    // ==================== 商家身份测试 ====================

    @Test
    @DisplayName("测试3: 选择商家身份成功-新商家开户")
    void testChooseUserRole_Merchant_NewMerchant() {
        Long userId = 2001L;
        Integer role = 1;

        when(userService.updateUserRole(userId, role))
            .thenReturn(Result.success("用户身份选择成功"));
        when(merchantMapper.selectCount(any())).thenReturn(0L);
        when(merchantMapper.insert(any(Merchant.class))).thenReturn(1);

        Result<String> result = roleService.chooseUserRole(userId, role);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("用户身份选择并初始化成功", result.getData());

        verify(userService, times(1)).updateUserRole(userId, role);
        verify(merchantMapper, times(1)).selectCount(any());
        verify(merchantMapper, times(1)).insert(any(Merchant.class));
        verify(customerMapper, never()).insert(any(Customer.class));
    }

    @Test
    @DisplayName("测试4: 选择商家身份成功-已有商家账户")
    void testChooseUserRole_Merchant_ExistingMerchant() {
        Long userId = 2002L;
        Integer role = 1;

        when(userService.updateUserRole(userId, role))
            .thenReturn(Result.success("用户身份选择成功"));
        when(merchantMapper.selectCount(any())).thenReturn(1L);

        Result<String> result = roleService.chooseUserRole(userId, role);

        assertEquals(200, result.getCode());
        assertEquals("用户身份选择并初始化成功", result.getData());

        verify(userService, times(1)).updateUserRole(userId, role);
        verify(merchantMapper, times(1)).selectCount(any());
        verify(merchantMapper, never()).insert(any(Merchant.class));
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("测试5: 用户不存在-更新角色失败")
    void testChooseUserRole_UserNotFound() {
        Long userId = 9999L;
        Integer role = 1;

        when(userService.updateUserRole(userId, role))
            .thenReturn(Result.error("用户不存在"));

        Result<String> result = roleService.chooseUserRole(userId, role);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("用户不存在，身份选择失败", result.getMessage());

        verify(userService, times(1)).updateUserRole(userId, role);
        verify(customerMapper, never()).selectCount(any());
        verify(merchantMapper, never()).selectCount(any());
        verify(customerMapper, never()).insert(any());
        verify(merchantMapper, never()).insert(any());
    }

    @Test
    @DisplayName("测试6: 无效角色值-不触发开户逻辑")
    void testChooseUserRole_InvalidRole() {
        Long userId = 1001L;
        Integer role = 99;

        when(userService.updateUserRole(userId, role))
            .thenReturn(Result.success("用户身份选择成功"));

        Result<String> result = roleService.chooseUserRole(userId, role);

        assertEquals(200, result.getCode());
        assertEquals("用户身份选择并初始化成功", result.getData());

        verify(userService, times(1)).updateUserRole(userId, role);
        verify(customerMapper, never()).selectCount(any());
        verify(merchantMapper, never()).selectCount(any());
        verify(customerMapper, never()).insert(any());
        verify(merchantMapper, never()).insert(any());
    }

    @Test
    @DisplayName("测试7: 验证客户账户初始余额为0")
    void testChooseUserRole_Customer_BalanceInitialization() {
        Long userId = 3001L;
        Integer role = 0;

        when(userService.updateUserRole(userId, role))
            .thenReturn(Result.success("用户身份选择成功"));
        when(customerMapper.selectCount(any())).thenReturn(0L);

        doAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            assertEquals(BigDecimal.ZERO, customer.getBalance());
            assertEquals(userId.intValue(), customer.getUserId());
            return 1;
        }).when(customerMapper).insert(any(Customer.class));

        Result<String> result = roleService.chooseUserRole(userId, role);

        assertEquals(200, result.getCode());
        verify(customerMapper, times(1)).insert(any(Customer.class));
    }

    @Test
    @DisplayName("测试8: 验证商家账户初始余额为0")
    void testChooseUserRole_Merchant_BalanceInitialization() {
        Long userId = 3002L;
        Integer role = 1;

        when(userService.updateUserRole(userId, role))
            .thenReturn(Result.success("用户身份选择成功"));
        when(merchantMapper.selectCount(any())).thenReturn(0L);

        doAnswer(invocation -> {
            Merchant merchant = invocation.getArgument(0);
            assertEquals(BigDecimal.ZERO, merchant.getBalance());
            assertEquals(userId.intValue(), merchant.getUserId());
            return 1;
        }).when(merchantMapper).insert(any(Merchant.class));

        Result<String> result = roleService.chooseUserRole(userId, role);

        assertEquals(200, result.getCode());
        verify(merchantMapper, times(1)).insert(any(Merchant.class));
    }
}
