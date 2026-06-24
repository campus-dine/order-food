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
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dishes") // 对应你的菜品表名
public class Dish {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dish_name")
    private String dishName;

    @TableField("image_url")
    private String imageUrl;

    @TableField("category_id")
    private Long categoryId; // 分类ID

    @TableField("rating")
    private BigDecimal rating; // 评分

    @TableField("stock")
    private Integer stock; // 库存

    @TableField("price")
    private BigDecimal price; // 售价

    @TableField("sales")
    private Integer sales; // 销量（用于流式排序的核心字段）

    @TableField("status")
    private Integer status; // 状态：0-下架，1-上架

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}