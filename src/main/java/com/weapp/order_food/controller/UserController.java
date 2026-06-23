package com.weapp.order_food.controller;

import com.weapp.order_food.model.dto.WeChatCodeDTO;
import com.weapp.order_food.service.UserService;
import com.weapp.order_food.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    
    @PostMapping("/login/wechat")
    public Result<String> loginWithWeChat(@javax.validation.Valid @RequestBody WeChatCodeDTO weChatCodeDTO) {
        return userService.loginWithWeChat(weChatCodeDTO.getCode());
    }

}
