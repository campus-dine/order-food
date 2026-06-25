package com.weapp.order_food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weapp.order_food.entity.Category;
import com.weapp.order_food.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("分类服务测试 - 获取所有分类")
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @SpyBean
    private CategoryServiceImpl categoryServiceImpl;

    @Test
    @DisplayName("测试1: 获取所有分类列表成功 - 按sort排序")
    void testGetAllCategoriesOrdered_Success() {
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

        Category cat3 = Category.builder()
                .id(3L)
                .categoryName("饮料小食")
                .sort(3)
                .createTime(LocalDateTime.now())
                .build();

        List<Category> mockCategories = Arrays.asList(cat1, cat2, cat3);

        doReturn(mockCategories).when(categoryServiceImpl).list(any(LambdaQueryWrapper.class));

        List<Category> result = categoryService.getAllCategoriesOrdered();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("热销推荐", result.get(0).getCategoryName());
        assertEquals("主食套餐", result.get(1).getCategoryName());
        assertEquals("饮料小食", result.get(2).getCategoryName());

        verify(categoryServiceImpl, times(1)).list(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试2: 获取分类列表 - 空列表")
    void testGetAllCategoriesOrdered_EmptyList() {
        doReturn(Arrays.asList()).when(categoryServiceImpl).list(any(LambdaQueryWrapper.class));

        List<Category> result = categoryService.getAllCategoriesOrdered();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categoryServiceImpl, times(1)).list(any(LambdaQueryWrapper.class));
    }
}
