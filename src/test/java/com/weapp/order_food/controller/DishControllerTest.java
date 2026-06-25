package com.weapp.order_food.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weapp.order_food.model.dto.DishDetailQueryDTO;
import com.weapp.order_food.model.dto.DishPageQueryDTO;
import com.weapp.order_food.model.vo.DishDetailVO;
import com.weapp.order_food.model.vo.DishScrollVO;
import com.weapp.order_food.service.DishService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DishController.class)
@DisplayName("菜品控制器测试 - API接口验证")
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DishService dishService;

    @Autowired
    private ObjectMapper objectMapper;

    private DishPageQueryDTO pageQueryDTO;
    private DishDetailQueryDTO detailQueryDTO;
    private DishScrollVO mockScrollVO;
    private DishDetailVO mockDetailVO;

    @BeforeEach
    void setUp() {
        pageQueryDTO = new DishPageQueryDTO();
        pageQueryDTO.setCategoryId(1L);
        pageQueryDTO.setLastId(0L);
        pageQueryDTO.setLastSales(0);

        detailQueryDTO = new DishDetailQueryDTO();
        detailQueryDTO.setDishId(101L);

        mockScrollVO = new DishScrollVO();
        mockScrollVO.setTotal(50L);
        mockScrollVO.setLastId(10L);
        mockScrollVO.setLastSales(100);
        mockScrollVO.setList(Arrays.asList());

        mockDetailVO = new DishDetailVO();
        mockDetailVO.setId(101L);
        mockDetailVO.setDishName("宫保鸡丁");
        mockDetailVO.setPrice(new BigDecimal("25.00"));
        mockDetailVO.setImageUrl("http://example.com/dish.jpg");
        mockDetailVO.setRating(new BigDecimal("4.8"));
        mockDetailVO.setStock(50);
        mockDetailVO.setCategoryId(1L);
    }

    @Test
    @DisplayName("测试1: 获取菜品滚动列表成功")
    void testGetCategoryDishScrollList_Success() throws Exception {
        when(dishService.getDishScrollListByCategory(any(DishPageQueryDTO.class))).thenReturn(mockScrollVO);

        mockMvc.perform(get("/api/customer/dish/scroll-list")
                .param("categoryId", "1")
                .param("lastId", "0")
                .param("lastSales", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(50));

        verify(dishService, times(1)).getDishScrollListByCategory(any(DishPageQueryDTO.class));
    }

    @Test
    @DisplayName("测试2: 获取菜品滚动列表失败 - 缺少categoryId")
    void testGetCategoryDishScrollList_MissingCategoryId() throws Exception {
        mockMvc.perform(get("/api/customer/dish/scroll-list")
                .param("lastId", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("分类ID不能为空"));

        verify(dishService, never()).getDishScrollListByCategory(any());
    }

    @Test
    @DisplayName("测试3: 获取菜品滚动列表失败 - 缺少lastId")
    void testGetCategoryDishScrollList_MissingLastId() throws Exception {
        mockMvc.perform(get("/api/customer/dish/scroll-list")
                .param("categoryId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("锚点ID不能为空"));

        verify(dishService, never()).getDishScrollListByCategory(any());
    }

    @Test
    @DisplayName("测试4: 获取菜品滚动列表 - lastSales默认为0")
    void testGetCategoryDishScrollList_DefaultLastSales() throws Exception {
        when(dishService.getDishScrollListByCategory(any(DishPageQueryDTO.class))).thenReturn(mockScrollVO);

        mockMvc.perform(get("/api/customer/dish/scroll-list")
                .param("categoryId", "1")
                .param("lastId", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(dishService, times(1)).getDishScrollListByCategory(argThat(dto ->
            dto != null && dto.getLastSales() == 0
        ));
    }

    @Test
    @DisplayName("测试5: 获取菜品详情成功")
    void testGetDishDetail_Success() throws Exception {
        when(dishService.getDishDetailWithRecipes(any(DishDetailQueryDTO.class))).thenReturn(mockDetailVO);

        mockMvc.perform(get("/api/customer/dish/detail")
                .param("dishId", "101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.dishName").value("宫保鸡丁"))
                .andExpect(jsonPath("$.data.price").value(25.00));

        verify(dishService, times(1)).getDishDetailWithRecipes(any(DishDetailQueryDTO.class));
    }

    @Test
    @DisplayName("测试6: 获取菜品详情失败 - 缺少dishId")
    void testGetDishDetail_MissingDishId() throws Exception {
        mockMvc.perform(get("/api/customer/dish/detail")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("菜品ID不能为空"));

        verify(dishService, never()).getDishDetailWithRecipes(any());
    }

    @Test
    @DisplayName("测试7: 获取菜品详情 - 菜品不存在")
    void testGetDishDetail_DishNotFound() throws Exception {
        when(dishService.getDishDetailWithRecipes(any(DishDetailQueryDTO.class))).thenReturn(null);

        mockMvc.perform(get("/api/customer/dish/detail")
                .param("dishId", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("该菜品不存在或已彻底下架"));

        verify(dishService, times(1)).getDishDetailWithRecipes(any(DishDetailQueryDTO.class));
    }

    @Test
    @DisplayName("测试8: 获取菜品详情失败 - 服务异常")
    void testGetDishDetail_ServiceException() throws Exception {
        when(dishService.getDishDetailWithRecipes(any(DishDetailQueryDTO.class)))
                .thenThrow(new RuntimeException("查询失败"));

        mockMvc.perform(get("/api/customer/dish/detail")
                .param("dishId", "101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("查询失败"));

        verify(dishService, times(1)).getDishDetailWithRecipes(any(DishDetailQueryDTO.class));
    }
}

