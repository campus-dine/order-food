package com.weapp.order_food.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "菜品详情查询参数")
public class DishDetailQueryDTO {
    @ApiModelProperty(value = "菜品ID", required = true)
    private Long dishId;
}