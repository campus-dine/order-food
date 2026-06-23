package com.weapp.order_food.service;


import com.weapp.order_food.utils.Result;

public interface RoleService {
    /**
     * 处理用户后置选择身份及对应的开户业务
     */
    Result<String> chooseUserRole(Long userId, Integer role);
}