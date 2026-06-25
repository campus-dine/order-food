package com.weapp.order_food.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderDishDTO {
    private Long dishId;
    private Integer quantity;
    private String remark;     // 客户单项菜品备注
}