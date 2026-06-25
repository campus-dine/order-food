package com.weapp.order_food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.weapp.order_food.entity.*;
import com.weapp.order_food.mapper.*;
import com.weapp.order_food.model.dto.*;
import com.weapp.order_food.model.vo.OrderDetailVO;
import com.weapp.order_food.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("订单服务测试 - 下单、状态流转与核销")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @SpyBean
    private OrderServiceImpl orderServiceImpl;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private OrderItemMapper orderItemMapper;

    @MockBean
    private OrderDeliveryMapper orderDeliveryMapper;

    @MockBean
    private CartMapper cartMapper;

    @MockBean
    private DishMapper dishMapper;

    // ==================== 提交订单测试 ====================

    @Test
    @DisplayName("测试1: 提交自取订单成功 - 校验库存并扣减")
    void testSubmitOrder_SelfPickup_Success() {
        Long customerId = 1001L;
        Long merchantId = 2001L;

        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setCustomerId(customerId);
        createDTO.setMerchantId(merchantId);
        createDTO.setDineType(0);
        createDTO.setIsDelivery(false);
        createDTO.setTotalAmount(new BigDecimal("50.00"));
        createDTO.setActualAmount(new BigDecimal("45.00"));

        OrderDishDTO dish1 = new OrderDishDTO();
        dish1.setDishId(101L);
        dish1.setQuantity(2);
        dish1.setRemark("不要辣");

        createDTO.setDishes(Arrays.asList(dish1));

        Cart cart = Cart.builder()
                .id(1L)
                .customerId(customerId)
                .dishId(101L)
                .quantity(2)
                .addedPrice(new BigDecimal("25.00"))
                .build();

        Dish dish = Dish.builder()
                .id(101L)
                .dishName("宫保鸡丁")
                .price(new BigDecimal("25.00"))
                .stock(10)
                .status(1)
                .build();

        when(cartMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(cart));
        when(dishMapper.selectById(101L)).thenReturn(dish);
        when(dishMapper.updateById(any(Dish.class))).thenReturn(1);
        when(cartMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        doReturn(true).when(orderServiceImpl).save(any(Order.class));
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        String orderNo = orderService.submitOrder(createDTO);

        assertNotNull(orderNo);
        assertTrue(orderNo.startsWith("ORD_"));
        verify(dishMapper, times(1)).updateById(argThat(d -> d.getStock() == 8));
        verify(cartMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(orderServiceImpl, times(1)).save(any(Order.class));
        verify(orderItemMapper, times(1)).insert(any(OrderItem.class));
        verify(orderDeliveryMapper, never()).insert(any(OrderDelivery.class));
    }

    @Test
    @DisplayName("测试2: 提交外送订单成功 - 包含配送信息")
    void testSubmitOrder_Delivery_Success() {
        Long customerId = 1001L;
        Long merchantId = 2001L;

        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setCustomerId(customerId);
        createDTO.setMerchantId(merchantId);
        createDTO.setDineType(0);
        createDTO.setIsDelivery(true);
        createDTO.setAddressId(301L);
        createDTO.setDeliveryFee(new BigDecimal("5.00"));
        createDTO.setTotalAmount(new BigDecimal("60.00"));
        createDTO.setActualAmount(new BigDecimal("55.00"));

        OrderDishDTO dish1 = new OrderDishDTO();
        dish1.setDishId(102L);
        dish1.setQuantity(1);
        createDTO.setDishes(Arrays.asList(dish1));

        Cart cart = Cart.builder()
                .id(2L)
                .customerId(customerId)
                .dishId(102L)
                .quantity(1)
                .build();

        Dish dish = Dish.builder()
                .id(102L)
                .dishName("红烧肉")
                .price(new BigDecimal("60.00"))
                .stock(5)
                .status(1)
                .build();

        when(cartMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(cart));
        when(dishMapper.selectById(102L)).thenReturn(dish);
        when(dishMapper.updateById(any(Dish.class))).thenReturn(1);
        when(cartMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        doReturn(true).when(orderServiceImpl).save(any(Order.class));
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(orderDeliveryMapper.insert(any(OrderDelivery.class))).thenReturn(1);

        String orderNo = orderService.submitOrder(createDTO);

        assertNotNull(orderNo);
        verify(orderDeliveryMapper, times(1)).insert(argThat(delivery ->
            delivery != null &&
            delivery.getAddressId().equals(301L) &&
            delivery.getDeliveryFee().compareTo(new BigDecimal("5.00")) == 0 &&
            delivery.getStatus() == 0
        ));
    }

    @Test
    @DisplayName("测试3: 提交订单失败 - 购物车数据不一致")
    void testSubmitOrder_CartDataMismatch_ThrowsException() {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setCustomerId(1001L);
        createDTO.setMerchantId(2001L);

        OrderDishDTO dish1 = new OrderDishDTO();
        dish1.setDishId(101L);
        dish1.setQuantity(3);
        createDTO.setDishes(Arrays.asList(dish1));

        Cart cart = Cart.builder()
                .id(1L)
                .customerId(1001L)
                .dishId(101L)
                .quantity(2)
                .build();

        when(cartMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(cart));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.submitOrder(createDTO);
        });

        assertEquals("操作失败：购物车数据不一致，请刷新后重试", exception.getMessage());
        verify(dishMapper, never()).selectById(anyLong());
        verify(orderServiceImpl, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("测试4: 提交订单失败 - 菜品已下架")
    void testSubmitOrder_DishOffline_ThrowsException() {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setCustomerId(1001L);
        createDTO.setMerchantId(2001L);

        OrderDishDTO dish1 = new OrderDishDTO();
        dish1.setDishId(101L);
        dish1.setQuantity(1);
        createDTO.setDishes(Arrays.asList(dish1));

        Cart cart = Cart.builder()
                .id(1L)
                .customerId(1001L)
                .dishId(101L)
                .quantity(1)
                .build();

        Dish offlineDish = Dish.builder()
                .id(101L)
                .dishName("下架菜品")
                .status(0)
                .build();

        when(cartMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(cart));
        when(dishMapper.selectById(101L)).thenReturn(offlineDish);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.submitOrder(createDTO);
        });

        assertEquals("操作失败：菜品已下架", exception.getMessage());
    }

    @Test
    @DisplayName("测试5: 提交订单失败 - 库存不足")
    void testSubmitOrder_InsufficientStock_ThrowsException() {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setCustomerId(1001L);
        createDTO.setMerchantId(2001L);

        OrderDishDTO dish1 = new OrderDishDTO();
        dish1.setDishId(101L);
        dish1.setQuantity(10);
        createDTO.setDishes(Arrays.asList(dish1));

        Cart cart = Cart.builder()
                .id(1L)
                .customerId(1001L)
                .dishId(101L)
                .quantity(10)
                .build();

        Dish dish = Dish.builder()
                .id(101L)
                .dishName("宫保鸡丁")
                .stock(3)
                .status(1)
                .build();

        when(cartMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(cart));
        when(dishMapper.selectById(101L)).thenReturn(dish);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.submitOrder(createDTO);
        });

        assertTrue(exception.getMessage().contains("库存不足"));
        verify(orderServiceImpl, never()).save(any(Order.class));
    }

    // ==================== 订单状态变更测试 ====================

    @Test
    @DisplayName("测试6: 变更订单状态成功 - 普通状态流转")
    void testChangeOrderStatus_NormalFlow_Success() {
        OrderStatusUpdateDTO statusDTO = new OrderStatusUpdateDTO();
        statusDTO.setOrderId(1L);
        statusDTO.setTargetStatus(2);
        statusDTO.setDeliveryStatus(null);

        doReturn(true).when(orderServiceImpl).update(any(com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper.class));

        orderService.changeOrderStatus(statusDTO);

        verify(orderServiceImpl, times(1)).update(any(com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper.class));
        verify(orderDeliveryMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("测试7: 变更订单状态成功 - 完成外送订单")
    void testChangeOrderStatus_CompleteDeliveryOrder_Success() {
        OrderStatusUpdateDTO statusDTO = new OrderStatusUpdateDTO();
        statusDTO.setOrderId(1L);
        statusDTO.setTargetStatus(4);
        statusDTO.setDeliveryStatus(2);

        OrderDelivery delivery = OrderDelivery.builder()
                .id(1L)
                .orderId(1L)
                .status(1)
                .build();

        when(orderDeliveryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(delivery);
        doReturn(true).when(orderServiceImpl).update(any(com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper.class));
        when(orderDeliveryMapper.update(isNull(), any(com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper.class))).thenReturn(1);

        orderService.changeOrderStatus(statusDTO);

        verify(orderServiceImpl, times(1)).update(any(com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper.class));
        verify(orderDeliveryMapper, times(1)).update(isNull(), any(com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper.class));
    }

    @Test
    @DisplayName("测试8: 变更订单状态失败 - 外送订单未完成配送不能确认完成")
    void testChangeOrderStatus_DeliveryNotStarted_ThrowsException() {
        OrderStatusUpdateDTO statusDTO = new OrderStatusUpdateDTO();
        statusDTO.setOrderId(1L);
        statusDTO.setTargetStatus(4);

        OrderDelivery delivery = OrderDelivery.builder()
                .id(1L)
                .orderId(1L)
                .status(0)
                .build();

        when(orderDeliveryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(delivery);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.changeOrderStatus(statusDTO);
        });

        assertEquals("外卖尚未开始配送，无法提前确认完成订单！", exception.getMessage());
        verify(orderServiceImpl, never()).update(any(LambdaUpdateWrapper.class));
    }

    // ==================== 商家核销测试 ====================

    @Test
    @DisplayName("测试9: 商家核销成功 - 自取订单直接完成")
    void testVerifyAndCompleteOrder_SelfPickup_Success() {
        MerchantVerifyCodeDTO verifyDTO = new MerchantVerifyCodeDTO();
        verifyDTO.setPickupCode("123456");
        verifyDTO.setMerchantId(2001L);

        Order order = Order.builder()
                .id(1L)
                .pickupCodeId("123456")
                .merchantId(2001L)
                .orderStatus(1)
                .build();

        doReturn(order).when(orderServiceImpl).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(orderServiceImpl).updateById(any(Order.class));

        String result = orderService.verifyAndCompleteOrder(verifyDTO);

        assertEquals("核销成功：该订单为自取/堂食，已直接完成订单！", result);
        verify(orderServiceImpl, times(1)).updateById(argThat(o -> o.getOrderStatus() == 4));
        verify(orderDeliveryMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("测试10: 商家核销成功 - 外送订单转为配送中")
    void testVerifyAndCompleteOrder_DeliveryOrder_Success() {
        MerchantVerifyCodeDTO verifyDTO = new MerchantVerifyCodeDTO();
        verifyDTO.setPickupCode("654321");
        verifyDTO.setMerchantId(2001L);

        Order order = Order.builder()
                .id(2L)
                .pickupCodeId("654321")
                .merchantId(2001L)
                .orderStatus(1)
                .build();

        OrderDelivery delivery = OrderDelivery.builder()
                .id(2L)
                .orderId(2L)
                .status(0)
                .build();

        doReturn(order).when(orderServiceImpl).getOne(any(LambdaQueryWrapper.class));
        when(orderDeliveryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(delivery);
        doReturn(true).when(orderServiceImpl).updateById(any(Order.class));
        when(orderDeliveryMapper.update(isNull(), any(com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper.class))).thenReturn(1);

        String result = orderService.verifyAndCompleteOrder(verifyDTO);

        assertEquals("验证成功：该订单为外送订单，已成功转为【配送中】状态，请安排配送！", result);
        verify(orderServiceImpl, times(1)).updateById(any(Order.class));
        verify(orderDeliveryMapper, times(1)).update(isNull(), any(com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper.class));
    }

    @Test
    @DisplayName("测试11: 商家核销失败 - 取餐码无效")
    void testVerifyAndCompleteOrder_InvalidCode_ThrowsException() {
        MerchantVerifyCodeDTO verifyDTO = new MerchantVerifyCodeDTO();
        verifyDTO.setPickupCode("999999");
        verifyDTO.setMerchantId(2001L);

        doReturn(null).when(orderServiceImpl).getOne(any(LambdaQueryWrapper.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.verifyAndCompleteOrder(verifyDTO);
        });

        assertEquals("核销失败：取餐码无效或订单已被处理", exception.getMessage());
    }

    // ==================== 历史订单查询测试 ====================

    @Test
    @DisplayName("测试12: 查询历史订单列表成功")
    void testGetHistoryOrderList_Success() {
        OrderHistoryQueryDTO queryDTO = new OrderHistoryQueryDTO();
        queryDTO.setCustomerId(1001L);

        Order order1 = Order.builder()
                .id(1L)
                .customerId(1001L)
                .orderNo("ORD_001")
                .createTime(LocalDateTime.now())
                .build();

        Order order2 = Order.builder()
                .id(2L)
                .customerId(1001L)
                .orderNo("ORD_002")
                .createTime(LocalDateTime.now().minusDays(1))
                .build();

        List<Order> mockOrders = Arrays.asList(order1, order2);

        doReturn(mockOrders).when(orderServiceImpl).list(any(LambdaQueryWrapper.class));

        List<Order> result = orderService.getHistoryOrderList(queryDTO);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ORD_001", result.get(0).getOrderNo());
        verify(orderServiceImpl, times(1)).list(any(LambdaQueryWrapper.class));
    }

    // ==================== 订单详情查询测试 ====================

    @Test
    @DisplayName("测试13: 查询订单详情成功 - 包含子表和配送信息")
    void testGetOrderDetailWithItems_Success() {
        OrderDetailQueryDTO queryDTO = new OrderDetailQueryDTO();
        queryDTO.setOrderId(1L);
        queryDTO.setStatus(3);

        Order order = Order.builder()
                .id(1L)
                .orderNo("ORD_123")
                .pickupCodeId("123456")
                .orderStatus(3)
                .dineType(0)
                .totalAmount(new BigDecimal("100.00"))
                .actualAmount(new BigDecimal("95.00"))
                .bookTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .dishId(101L)
                .quantity(2)
                .dishName("宫保鸡丁")
                .payPrice(new BigDecimal("50.00"))
                .remark("微辣")
                .build();

        OrderDelivery delivery = OrderDelivery.builder()
                .id(1L)
                .orderId(1L)
                .status(1)
                .build();

        doReturn(order).when(orderServiceImpl).getOne(any(LambdaQueryWrapper.class));
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(item1));
        when(orderDeliveryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(delivery);

        OrderDetailVO vo = orderService.getOrderDetailWithItems(queryDTO);

        assertNotNull(vo);
        assertEquals("ORD_123", vo.getOrderNo());
        assertEquals(3, vo.getOrderStatus());
        assertNotNull(vo.getOrderItems());
        assertEquals(1, vo.getOrderItems().size());
        assertEquals(1, vo.getDeliveryStatus());
    }

    @Test
    @DisplayName("测试14: 查询订单详情 - 状态不符返回null")
    void testGetOrderDetailWithItems_StatusMismatch_ReturnsNull() {
        OrderDetailQueryDTO queryDTO = new OrderDetailQueryDTO();
        queryDTO.setOrderId(1L);
        queryDTO.setStatus(3);

        doReturn(null).when(orderServiceImpl).getOne(any(LambdaQueryWrapper.class));

        OrderDetailVO vo = orderService.getOrderDetailWithItems(queryDTO);

        assertNull(vo);
        verify(orderItemMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试15: 查询订单详情 - 无配送信息时deliveryStatus为null")
    void testGetOrderDetailWithItems_NoDelivery_ReturnsNullDeliveryStatus() {
        OrderDetailQueryDTO queryDTO = new OrderDetailQueryDTO();
        queryDTO.setOrderId(1L);
        queryDTO.setStatus(1);

        Order order = Order.builder()
                .id(1L)
                .orderNo("ORD_456")
                .orderStatus(1)
                .dineType(0)
                .totalAmount(new BigDecimal("50.00"))
                .actualAmount(new BigDecimal("45.00"))
                .build();

        doReturn(order).when(orderServiceImpl).getOne(any(LambdaQueryWrapper.class));
        when(orderItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());
        when(orderDeliveryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        OrderDetailVO vo = orderService.getOrderDetailWithItems(queryDTO);

        assertNotNull(vo);
        assertNull(vo.getDeliveryStatus());
    }
}
