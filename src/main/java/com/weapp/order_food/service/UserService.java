package com.weapp.order_food.service;


import com.weapp.order_food.utils.Result;

public interface UserService {
     Result<String> loginWithWeChat(String code);
     Result<String>  updateUserRole(Long userId,Integer role);
}
