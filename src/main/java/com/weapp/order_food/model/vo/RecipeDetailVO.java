package com.weapp.order_food.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecipeDetailVO {
    private Long ingredientId;    // 配料ID
    private String ingredientName;// 配料名称（从配料表连表查出）
    private BigDecimal usageAmount;// 用量
    private String usageUnit;     // 用量单位
    private BigDecimal cost;      // 成本
}