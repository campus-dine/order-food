package com.weapp.order_food.controller;


import com.weapp.order_food.model.dto.DishDetailQueryDTO;
import com.weapp.order_food.model.dto.DishPageQueryDTO;
import com.weapp.order_food.model.vo.DishDetailVO;
import com.weapp.order_food.model.vo.DishScrollVO;
import com.weapp.order_food.service.DishService;
import com.weapp.order_food.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "客户菜品展示模块")
@RestController
@RequestMapping("/api/customer/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @ApiOperation("根据分类流式滚动获取菜品列表（高销量优先）")
    @GetMapping("/scroll-list")
    public Result<DishScrollVO> getCategoryDishScrollList(DishPageQueryDTO dishPageQueryDTO) {

        // 1. 验证 DTO 参数
        if (dishPageQueryDTO.getCategoryId() == null) {
            return Result.error("分类ID不能为空");
        }
        if (dishPageQueryDTO.getLastId() == null) {
            return Result.error("锚点ID不能为空");
        }
        if (dishPageQueryDTO.getLastSales() == null) {
            dishPageQueryDTO.setLastSales(0); // 赋予默认值
        }

        // 2. 调用 Service 层，将 DTO 传过去
        DishScrollVO scrollVO = dishService.getDishScrollListByCategory(dishPageQueryDTO);

        // 3. 统一使用 Result 向上返回
        return Result.success(scrollVO);
    }
    @ApiOperation("获取特定菜品的详细信息（含配方配料表）")
    @GetMapping("/detail")
    public Result<DishDetailVO> getDishDetail(DishDetailQueryDTO dto) {
        if (dto.getDishId() == null) {
            return Result.error("菜品ID不能为空");
        }

        try {
            // 调用 Service 层处理
            DishDetailVO dishDetailVO = dishService.getDishDetailWithRecipes(dto);

            if (dishDetailVO == null) {
                return Result.error("该菜品不存在或已彻底下架");
            }

            return Result.success(dishDetailVO);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }

    }
}