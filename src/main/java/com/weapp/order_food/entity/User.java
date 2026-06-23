package com.weapp.order_food.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("users")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("open_id")
    private String openId;

    @TableField("username")
    private String username;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("role")
    private String role;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

}
