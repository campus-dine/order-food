package com.weapp.order_food.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WeChatCodeDTO {
    @NotBlank(message = "code不能为空")
    private String code;
//    private Integer role;
}
