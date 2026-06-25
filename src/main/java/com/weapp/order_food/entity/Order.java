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
@TableName("orders")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("customer_id")
    private Long customerId;

    @TableField("merchant_id")
    private Long merchantId;

    @TableField("order_no")
    private String orderNo;

    @TableField("pickup_code_id")
    private String pickupCodeId;

    @TableField("book_time")
    private LocalDateTime bookTime;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("actual_amount")
    private BigDecimal actualAmount;

    /**
     * 0-待支付, 1-待制作/接单, 2-制作中, 3-待自取/配送中, 4-已完成, 5-已取消
     */
    @TableField("order_status")
    private Integer orderStatus;

    /**
     * 0-即时，1-预约订单
     */
    @TableField("dine_type")
    private Integer dineType;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}