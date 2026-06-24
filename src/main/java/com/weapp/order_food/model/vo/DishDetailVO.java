package com.weapp.order_food.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DishDetailVO {
    private Long id;
    private String dishName;
    private String imageUrl;
    private Long categoryId;
    private BigDecimal rating;
    private Integer stock;
    private BigDecimal price;

    // 🚨 核心组合：该菜品对应的配方表+配料表详情清单
    private List<RecipeDetailVO> recipes;
}