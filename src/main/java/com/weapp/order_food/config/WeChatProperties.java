package com.weapp.order_food.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "wx.miniapp")
public class WeChatProperties {
    private String appId;
    private String secret;
}
