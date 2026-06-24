package com.weapp.order_food.controller;


import com.weapp.order_food.entity.Category;
import com.weapp.order_food.service.CategoryService;
import com.weapp.order_food.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "客户菜品分类模块")
@RestController
@RequestMapping("/api/customer/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @ApiOperation("查询所有菜品分类列表（按sort升序）")
    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        try {
            // 调用业务层获取排好序的分类列表
            List<Category> list = categoryService.getAllCategoriesOrdered();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

    }
}