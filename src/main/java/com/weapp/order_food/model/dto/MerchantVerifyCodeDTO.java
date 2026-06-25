package com.weapp.order_food.model.dto;

import lombok.Data;

@Data
public class MerchantVerifyCodeDTO {
    private String pickupCode; // 商家输入的6位取餐码
    private Long merchantId;   // 当前操作的商家ID
}