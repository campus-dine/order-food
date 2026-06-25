package com.weapp.order_food.model.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderCreateDTO {
    private Long customerId;
    private Long merchantId;
    private Integer dineType;          // 0-即时，1-预约
    private LocalDateTime bookTime;    // 预约时间
    private BigDecimal totalAmount;
    private BigDecimal actualAmount;

    // 🚨 决定是不是外送的核心标志
    private Boolean isDelivery;        // true-是外送订单，false-自取/堂食
    private Long addressId;            // 如果是外送，必传地址id
    private BigDecimal deliveryFee;    // 配送费

    // 选中的商品列表
    private List<OrderDishDTO> dishes;
}