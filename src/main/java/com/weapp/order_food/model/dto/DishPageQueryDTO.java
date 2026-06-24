package com.weapp.order_food.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "客户菜品流式分页查询参数")
public class DishPageQueryDTO {

    @ApiModelProperty(value = "分类ID", required = true)
    private Long categoryId;

    @ApiModelProperty(value = "前端已展示列表的最后一个菜品ID（初次进入或刷新传 -1）", required = true)
    private Long lastId;

    @ApiModelProperty(value = "前端已展示列表的最后一个菜品销量（初次进入或刷新传 0）", required = false)
    private Integer lastSales;
}