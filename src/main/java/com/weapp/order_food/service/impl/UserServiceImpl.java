package com.weapp.order_food.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.weapp.order_food.config.WeChatProperties;
import com.weapp.order_food.entity.User;
import com.weapp.order_food.mapper.UserMapper;
import com.weapp.order_food.service.UserService;
import com.weapp.order_food.utils.HttpClientUtil;
import com.weapp.order_food.utils.JwtTokenUtil;
import com.weapp.order_food.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final WeChatProperties weChatProperties;

    private static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Override
    public Result<String> loginWithWeChat(String code) {
        String openId = getOpenId(code);
        if (openId == null) {
            return Result.error("微信授权失败");
        }
        Long userId = userMapper.getUserByOpenId(openId);

        if (userId == null) {
            // 新用户：自动注册
            User user = User.builder().openId(openId).build();

            // 🚨 注意：必须确保该自定义 insertUsers 方法在 Mapper 中配置了主键回填！
            // 或者如果是 MyBatis-Plus，直接改用：this.save(user); (需要当前类继承 ServiceImpl)
            userMapper.insertUsers(user);

            log.info("新用户注册成功，自动生成的主键ID为: {}", user.getId());

            String token = JwtTokenUtil.generateTokenWithUserId(user.getId());
            return Result.success("登录成功", token);
        }
        return Result.success("登录成功", JwtTokenUtil.generateTokenWithUserId(userId));
    }

    public String getOpenId(String code) {
        // 封装请求参数
        HashMap<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppId());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

        try {
            // 使用 HttpClientUtil 发送请求
            String json = HttpClientUtil.doGet(WECHAT_LOGIN_URL, map);
            log.info("微信服务器返回的原始数据: {}", json);

            // 解析数据
            JSONObject jsonObject = JSON.parseObject(json);

            //  检查微信官方是否返回了错误码
            if (jsonObject.containsKey("errcode") && jsonObject.getInteger("errcode") != 0) {
                log.error("微信接口返回异常码: {}, 错误原因: {}", jsonObject.getInteger("errcode"), jsonObject.getString("errmsg"));
                return null;
            }

            return jsonObject.getString("openid");
        } catch (Exception e) {
            log.error("请求微信服务器发生系统异常", e);
            return null;
        }
    }

}
