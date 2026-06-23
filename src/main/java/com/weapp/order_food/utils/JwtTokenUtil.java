package com.weapp.order_food.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component // 🚨 必须加这个注解，Spring 才能帮你把 yml 的配置注入进来
public class JwtTokenUtil {

    private static SecretKey javaSecretKey;
    private static long expirationTime;

    // 1. 通过构造函数，把 application.yml 里的 jwt 参数自动读进来
    public JwtTokenUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {

        // JJWT 0.12.x 要求必须将字符串密钥转化为标准的 SecretKey 对象
        javaSecretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        expirationTime = expiration;
    }

    /**
     * 生成携带用户 id 的 token
     * @param userId 用户ID
     * @return 生成的 JWT 字符串
     */
    public static String generateTokenWithUserId(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("userId", userId) // 载荷：存入用户ID
                .issuedAt(new Date(now)) // 签发时间
                .expiration(new Date(now + expirationTime)) // 过期时间
                .signWith(javaSecretKey) // 签名加密
                .compact();
    }

    /**
     * 解析 token 并返回用户 id
     * @param token 前端传过来的token
     * @return 用户ID的字符串形式
     */
    public static String parseTokenGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(javaSecretKey) // 验证签名
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 提取出 userId 并转成 String 返回
            return String.valueOf(claims.get("userId"));
        } catch (Exception e) {
            // 如果 token 过期或者被篡改，这里会直接抛出异常，拦截器可以捕获
            throw new RuntimeException("Token解析失败或已过期！");
        }
    }


//    public static void main(String[] args) {
//        String token = generateTokenWithUserId(123L);
//        System.out.println("生成的 Token: " + token);
//        Long userId = parseTokenGetUserId(token);
//        System.out.println("解析出的 userId: " + userId);
//    }
}