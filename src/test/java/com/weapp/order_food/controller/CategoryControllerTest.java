package com.weapp.order_food.controller;

import com.weapp.order_food.entity.Category;
import com.weapp.order_food.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("分类控制器测试 - API接口验证")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private List<Category> mockCategoryList;

    @BeforeEach
    void setUp() {
        Category cat1 = Category.builder()
                .id(1L)
                .categoryName("热销推荐")
                .sort(1)
                .createTime(LocalDateTime.now())
                .build();

        Category cat2 = Category.builder()
                .id(2L)
                .categoryName("主食套餐")
                .sort(2)
                .createTime(LocalDateTime.now())
                .build();

        mockCategoryList = Arrays.asList(cat1, cat2);
    }

    @Test
    @DisplayName("测试1: 获取分类列表成功")
    void testGetCategoryList_Success() throws Exception {
        when(categoryService.getAllCategoriesOrdered()).thenReturn(mockCategoryList);

        mockMvc.perform(get("/api/customer/category/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].categoryName").value("热销推荐"))
                .andExpect(jsonPath("$.data[1].categoryName").value("主食套餐"));

        verify(categoryService, times(1)).getAllCategoriesOrdered();
    }

    @Test
    @DisplayName("测试2: 获取分类列表 - 空列表")
    void testGetCategoryList_EmptyList() throws Exception {
        when(categoryService.getAllCategoriesOrdered()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/customer/category/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(categoryService, times(1)).getAllCategoriesOrdered();
    }

    @Test
    @DisplayName("测试3: 获取分类列表失败 - 服务异常")
    void testGetCategoryList_ServiceException() throws Exception {
        when(categoryService.getAllCategoriesOrdered()).thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/api/customer/category/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("数据库连接失败"));

        verify(categoryService, times(1)).getAllCategoriesOrdered();
    }
}
