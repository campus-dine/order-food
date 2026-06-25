package com.weapp.order_food.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_items")
public class OrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("dish_id")
    private Long dishId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("dish_name")
    private String dishName;

    @TableField("pay_price")
    private BigDecimal payPrice;

    @TableField("remark")
    private String remark;
}