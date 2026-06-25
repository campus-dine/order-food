package com.weapp.order_food.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weapp.order_food.entity.Order;
import com.weapp.order_food.model.dto.*;
import com.weapp.order_food.model.vo.OrderDetailVO;
import com.weapp.order_food.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("订单控制器测试 - API接口验证")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderCreateDTO createDTO;
    private OrderStatusUpdateDTO statusUpdateDTO;
    private MerchantVerifyCodeDTO verifyCodeDTO;
    private OrderHistoryQueryDTO historyQueryDTO;
    private OrderDetailQueryDTO detailQueryDTO;
    private List<Order> mockOrderList;
    private OrderDetailVO mockDetailVO;

    @BeforeEach
    void setUp() {
        OrderDishDTO dishDTO = new OrderDishDTO();
        dishDTO.setDishId(101L);
        dishDTO.setQuantity(2);
        dishDTO.setRemark("不要辣");

        createDTO = new OrderCreateDTO();
        createDTO.setCustomerId(1001L);
        createDTO.setMerchantId(2001L);
        createDTO.setDineType(0);
        createDTO.setIsDelivery(false);
        createDTO.setTotalAmount(new BigDecimal("50.00"));
        createDTO.setActualAmount(new BigDecimal("45.00"));
        createDTO.setDishes(Arrays.asList(dishDTO));

        statusUpdateDTO = new OrderStatusUpdateDTO();
        statusUpdateDTO.setOrderId(1L);
        statusUpdateDTO.setTargetStatus(2);

        verifyCodeDTO = new MerchantVerifyCodeDTO();
        verifyCodeDTO.setPickupCode("123456");
        verifyCodeDTO.setMerchantId(2001L);

        historyQueryDTO = new OrderHistoryQueryDTO();
        historyQueryDTO.setCustomerId(1001L);

        detailQueryDTO = new OrderDetailQueryDTO();
        detailQueryDTO.setOrderId(1L);
        detailQueryDTO.setStatus(3);

        Order order1 = Order.builder()
                .id(1L)
                .orderNo("ORD_001")
                .customerId(1001L)
                .orderStatus(4)
                .totalAmount(new BigDecimal("50.00"))
                .createTime(LocalDateTime.now())
                .build();

        Order order2 = Order.builder()
                .id(2L)
                .orderNo("ORD_002")
                .customerId(1001L)
                .orderStatus(3)
                .totalAmount(new BigDecimal("30.00"))
                .createTime(LocalDateTime.now().minusDays(1))
                .build();

        mockOrderList = Arrays.asList(order1, order2);

        mockDetailVO = new OrderDetailVO();
        mockDetailVO.setId(1L);
        mockDetailVO.setOrderNo("ORD_001");
        mockDetailVO.setPickupCodeId("123456");
        mockDetailVO.setOrderStatus(3);
        mockDetailVO.setTotalAmount(new BigDecimal("50.00"));
        mockDetailVO.setActualAmount(new BigDecimal("45.00"));
    }

    @Test
    @DisplayName("测试1: 创建订单成功 - 自取订单")
    void testCreateOrder_SelfPickup_Success() throws Exception {
        when(orderService.submitOrder(any(OrderCreateDTO.class))).thenReturn("ORD_1234567890");

        mockMvc.perform(post("/api/customer/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value("ORD_1234567890"));

        verify(orderService, times(1)).submitOrder(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("测试2: 创建外送订单成功")
    void testCreateOrder_Delivery_Success() throws Exception {
        createDTO.setIsDelivery(true);
        createDTO.setAddressId(301L);
        createDTO.setDeliveryFee(new BigDecimal("5.00"));

        when(orderService.submitOrder(any(OrderCreateDTO.class))).thenReturn("ORD_1234567890");

        mockMvc.perform(post("/api/customer/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("ORD_1234567890"));

        verify(orderService, times(1)).submitOrder(any(OrderCreateDTO.class));
    }

    @Test
    @DisplayName("测试3: 创建订单失败 - 缺少商品")
    void testCreateOrder_MissingDishes() throws Exception {
        OrderCreateDTO emptyDTO = new OrderCreateDTO();
        emptyDTO.setCustomerId(1001L);

        mockMvc.perform(post("/api/customer/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("订单商品不能为空"));

        verify(orderService, never()).submitOrder(any());
    }

    @Test
    @DisplayName("测试4: 创建外送订单失败 - 缺少地址")
    void testCreateOrder_Delivery_MissingAddress() throws Exception {
        createDTO.setIsDelivery(true);

        mockMvc.perform(post("/api/customer/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("外送订单缺少配送地址"));

        verify(orderService, never()).submitOrder(any());
    }

    @Test
    @DisplayName("测试5: 变更订单状态成功")
    void testUpdateOrderStatus_Success() throws Exception {
        doNothing().when(orderService).changeOrderStatus(any(OrderStatusUpdateDTO.class));

        mockMvc.perform(put("/api/customer/order/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(orderService, times(1)).changeOrderStatus(any(OrderStatusUpdateDTO.class));
    }

    @Test
    @DisplayName("测试6: 变更订单状态失败 - 缺少参数")
    void testUpdateOrderStatus_MissingParameters() throws Exception {
        OrderStatusUpdateDTO incompleteDTO = new OrderStatusUpdateDTO();
        incompleteDTO.setOrderId(1L);

        mockMvc.perform(put("/api/customer/order/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("参数不完整"));

        verify(orderService, never()).changeOrderStatus(any());
    }

    @Test
    @DisplayName("测试7: 商家核销成功 - 自取订单")
    void testVerifyPickupCode_SelfPickup_Success() throws Exception {
        when(orderService.verifyAndCompleteOrder(any(MerchantVerifyCodeDTO.class)))
                .thenReturn("核销成功：该订单为自取/堂食，已直接完成订单！");

        mockMvc.perform(put("/api/customer/order/merchant/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("核销成功：该订单为自取/堂食，已直接完成订单！"));

        verify(orderService, times(1)).verifyAndCompleteOrder(any(MerchantVerifyCodeDTO.class));
    }

    @Test
    @DisplayName("测试8: 商家核销成功 - 外送订单")
    void testVerifyPickupCode_Delivery_Success() throws Exception {
        when(orderService.verifyAndCompleteOrder(any(MerchantVerifyCodeDTO.class)))
                .thenReturn("验证成功：该订单为外送订单，已成功转为【配送中】状态，请安排配送！");

        mockMvc.perform(put("/api/customer/order/merchant/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("验证成功：该订单为外送订单，已成功转为【配送中】状态，请安排配送！"));

        verify(orderService, times(1)).verifyAndCompleteOrder(any(MerchantVerifyCodeDTO.class));
    }

    @Test
    @DisplayName("测试9: 商家核销失败 - 缺少取餐码")
    void testVerifyPickupCode_MissingPickupCode() throws Exception {
        MerchantVerifyCodeDTO incompleteDTO = new MerchantVerifyCodeDTO();
        incompleteDTO.setMerchantId(2001L);

        mockMvc.perform(put("/api/customer/order/merchant/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("取餐码或商家信息不能为空"));

        verify(orderService, never()).verifyAndCompleteOrder(any());
    }

    @Test
    @DisplayName("测试10: 查询历史订单列表成功")
    void testGetHistoryList_Success() throws Exception {
        when(orderService.getHistoryOrderList(any(OrderHistoryQueryDTO.class))).thenReturn(mockOrderList);

        mockMvc.perform(get("/api/customer/order/history-list")
                .param("customerId", "1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderNo").value("ORD_001"));

        verify(orderService, times(1)).getHistoryOrderList(any(OrderHistoryQueryDTO.class));
    }

    @Test
    @DisplayName("测试11: 查询历史订单列表失败 - 缺少customerId")
    void testGetHistoryList_MissingCustomerId() throws Exception {
        mockMvc.perform(get("/api/customer/order/history-list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("客户ID不能为空"));

        verify(orderService, never()).getHistoryOrderList(any());
    }

    @Test
    @DisplayName("测试12: 查询订单详情成功")
    void testGetOrderDetail_Success() throws Exception {
        when(orderService.getOrderDetailWithItems(any(OrderDetailQueryDTO.class))).thenReturn(mockDetailVO);

        mockMvc.perform(get("/api/customer/order/detail")
                .param("orderId", "1")
                .param("status", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("ORD_001"))
                .andExpect(jsonPath("$.data.orderStatus").value(3));

        verify(orderService, times(1)).getOrderDetailWithItems(any(OrderDetailQueryDTO.class));
    }

    @Test
    @DisplayName("测试13: 查询订单详情失败 - 缺少参数")
    void testGetOrderDetail_MissingParameters() throws Exception {
        mockMvc.perform(get("/api/customer/order/detail")
                .param("orderId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("请求参数不完整"));

        verify(orderService, never()).getOrderDetailWithItems(any());
    }

    @Test
    @DisplayName("测试14: 查询订单详情 - 订单不存在或状态不符")
    void testGetOrderDetail_OrderNotFound() throws Exception {
        when(orderService.getOrderDetailWithItems(any(OrderDetailQueryDTO.class))).thenReturn(null);

        mockMvc.perform(get("/api/customer/order/detail")
                .param("orderId", "999")
                .param("status", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("未找到符合状态要求的订单"));

        verify(orderService, times(1)).getOrderDetailWithItems(any(OrderDetailQueryDTO.class));
    }
}
