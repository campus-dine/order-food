package com.weapp.order_food.controller;

import com.weapp.order_food.model.dto.WeChatCodeDTO;
import com.weapp.order_food.service.RoleService;
import com.weapp.order_food.service.UserService;
import com.weapp.order_food.utils.JwtTokenUtil;
import com.weapp.order_food.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "用户管理模块")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @PostMapping("/login/wechat")
    public Result<String> loginWithWeChat(@Valid @RequestBody WeChatCodeDTO weChatCodeDTO) {
//        if (weChatCodeDTO.getRole() != 0 && weChatCodeDTO.getRole() != 1) {
//            return Result.error("身份参数非法");
//        }
        return userService.loginWithWeChat(weChatCodeDTO.getCode());
    }

    @ApiOperation("选择/切换用户身份")
    @PutMapping("/role")
    public Result<String> chooseRole(
            @RequestHeader("Authorization") String token, // 从请求头获取Token
            @RequestParam Integer role                   // 前端传来的身份代码：0-客户/学生，1-商家
    ) {
        // 1. 严格校验身份参数是否合法
        if (role != 0 && role != 1) {
            return Result.error("身份参数不正确");
        }

        try {
            // 2. 解析 Token 拿到当前登录用户的数据库自增 ID
            String userIdStr = JwtTokenUtil.parseTokenGetUserId(token);
            Long userId = Long.parseLong(userIdStr);

            // 3. 调用 Service 层修改数据库中的身份
            if (roleService.chooseUserRole(userId, role).getCode() == 200) {
                String newToken = JwtTokenUtil.generateToken(userId, role);
                return Result.success("身份选择成功", newToken);
            }else {
                return Result.error("身份选择失败");
            }
        } catch (Exception e) {
            return Result.error("身份选择失败: " + e.getMessage());
        }
    }

}
