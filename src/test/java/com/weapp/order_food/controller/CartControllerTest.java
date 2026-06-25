package com.weapp.order_food.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weapp.order_food.model.dto.CartOperationDTO;
import com.weapp.order_food.model.dto.CartQueryDTO;
import com.weapp.order_food.model.vo.CartVO;
import com.weapp.order_food.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@DisplayName("购物车控制器测试 - API接口验证")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private CartQueryDTO queryDTO;
    private CartOperationDTO operationDTO;
    private List<CartVO> mockCartList;

    @BeforeEach
    void setUp() {
        queryDTO = new CartQueryDTO();
        queryDTO.setCustomerId(1001L);

        operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(101L);

        CartVO vo1 = new CartVO();
        vo1.setId(1L);
        vo1.setDishId(101L);
        vo1.setDishName("宫保鸡丁");
        vo1.setAddedPrice(new BigDecimal("25.00"));
        vo1.setQuantity(2);

        CartVO vo2 = new CartVO();
        vo2.setId(2L);
        vo2.setDishId(102L);
        vo2.setDishName("麻婆豆腐");
        vo2.setAddedPrice(new BigDecimal("18.00"));
        vo2.setQuantity(1);

        mockCartList = Arrays.asList(vo1, vo2);
    }

    @Test
    @DisplayName("测试1: 查询购物车列表成功")
    void testGetCartList_Success() throws Exception {
        when(cartService.getCartListByCustomerId(any(CartQueryDTO.class))).thenReturn(mockCartList);

        mockMvc.perform(get("/api/customer/cart/list")
                .param("customerId", "1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].dishName").value("宫保鸡丁"));

        verify(cartService, times(1)).getCartListByCustomerId(any(CartQueryDTO.class));
    }

    @Test
    @DisplayName("测试2: 查询购物车列表失败 - 缺少customerId")
    void testGetCartList_MissingCustomerId() throws Exception {
        mockMvc.perform(get("/api/customer/cart/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("客户ID不能为空"));

        verify(cartService, never()).getCartListByCustomerId(any());
    }

    @Test
    @DisplayName("测试3: 加入购物车成功")
    void testAddIntoCart_Success() throws Exception {
        when(cartService.addQuantity(any(CartOperationDTO.class))).thenReturn(mockCartList);

        mockMvc.perform(post("/api/customer/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(operationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(cartService, times(1)).addQuantity(any(CartOperationDTO.class));
    }

    @Test
    @DisplayName("测试4: 加入购物车失败 - 缺少dishId")
    void testAddIntoCart_MissingDishId() throws Exception {
        CartOperationDTO incompleteDTO = new CartOperationDTO();
        incompleteDTO.setCustomerId(1001L);

        mockMvc.perform(post("/api/customer/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("参数不完整"));

        verify(cartService, never()).addQuantity(any());
    }

    @Test
    @DisplayName("测试5: 减少购物车商品成功")
    void testSubFromCart_Success() throws Exception {
        when(cartService.subQuantity(any(CartOperationDTO.class))).thenReturn(mockCartList);

        mockMvc.perform(post("/api/customer/cart/sub")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(operationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(cartService, times(1)).subQuantity(any(CartOperationDTO.class));
    }

    @Test
    @DisplayName("测试6: 减少购物车商品失败 - 缺少customerId")
    void testSubFromCart_MissingCustomerId() throws Exception {
        CartOperationDTO incompleteDTO = new CartOperationDTO();
        incompleteDTO.setDishId(101L);

        mockMvc.perform(post("/api/customer/cart/sub")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("参数不完整"));

        verify(cartService, never()).subQuantity(any());
    }
}
