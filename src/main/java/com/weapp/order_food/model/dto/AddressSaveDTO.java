package com.weapp.order_food.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "地址保存与修改参数")
public class AddressSaveDTO {
    private Long id;             // 地址ID（修改、设置常用时必传；新增时前端传 null）
    private Long customerId;     // 客户ID
    private String detailAddress;// 具体地址
    private Integer isDefault;   // 是否常用：1-常用，0-普通
}