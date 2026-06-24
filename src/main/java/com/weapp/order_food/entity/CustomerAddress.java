package com.weapp.order_food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("customer_addresses")
public class CustomerAddress {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("customer_id")
    private Long customerId;
    @TableField("detail_address")
    private String detailAddress;
    @TableField("is_default")
    private Integer isDefault; // 在MyBatis-Plus中映射，Java的null会落库为MySQL的NULL
}