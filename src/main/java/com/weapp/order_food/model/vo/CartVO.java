package com.weapp.order_food.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartVO {
    private Long id;
    private Long dishId;
    private String dishName;     // 连表或者额外查出来的菜名
    private String imageUrl;     // 菜品图片URL
    private BigDecimal addedPrice;// 加入时的单价
    private Integer quantity;    // 数量
}