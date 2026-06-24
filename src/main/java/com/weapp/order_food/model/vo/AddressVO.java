package com.weapp.order_food.model.vo;

import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private Long customerId;
    private String detailAddress;
    private Integer isDefault; // 映射给前端时，把 NULL 转回 0，方便前端判断
}