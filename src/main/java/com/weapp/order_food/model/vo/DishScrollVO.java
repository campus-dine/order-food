package com.weapp.order_food.model.vo;

import com.weapp.order_food.entity.Dish;
import lombok.Data;
import java.util.List;

@Data
public class DishScrollVO {
    private List<Dish> list;      // 这次返回的 10 条菜品数据
    private Long total;           // 当前分类下的菜品总数（用于前端展示或判断）
    private Long lastId;          // 这次返回的最后一条菜品ID（初次或无数据返回 -1）
    private Integer lastSales;    // 这次返回的最后一条菜品销量（初次或无数据返回 0）
}