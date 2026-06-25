package com.weapp.order_food.model.vo;

import com.weapp.order_food.entity.OrderItem;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private String pickupCodeId;
    private Integer orderStatus;
    private Integer dineType;
    private BigDecimal totalAmount;
    private BigDecimal actualAmount;
    private LocalDateTime bookTime;
    private LocalDateTime createTime;

    // 🚨 核心组合：这个订单下面的所有菜品明细子表
    private List<OrderItem> orderItems;

    // 如果是外送订单，额外返回外送状态，非外送为 null
    private Integer deliveryStatus;
}