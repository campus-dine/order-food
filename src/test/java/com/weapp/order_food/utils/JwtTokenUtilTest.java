package com.weapp.order_food.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenUtil 工具类测试")
class JwtTokenUtilTest {

    private static final String TEST_SECRET = "mySecretKeyForJwtTesting123456789";
    private static final long TEST_EXPIRATION = 3600000L;

    @BeforeAll
    static void setUp() {
        ReflectionTestUtils.setField(JwtTokenUtil.class, "javaSecretKey",
            io.jsonwebtoken.security.Keys.hmacShaKeyFor(TEST_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        ReflectionTestUtils.setField(JwtTokenUtil.class, "expirationTime", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("测试1: 生成Token成功")
    void testGenerateToken_Success() {
        String token = JwtTokenUtil.generateToken(1001L, 0);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("测试2: 解析Token获取用户ID")
    void testParseTokenGetUserId_Success() {
        Long userId = 1234L;
        String token = JwtTokenUtil.generateToken(userId, 1);

        String parsedUserId = JwtTokenUtil.parseTokenGetUserId(token);

        assertEquals(String.valueOf(userId), parsedUserId);
    }

    @Test
    @DisplayName("测试3: 解析Token获取角色")
    void testParseTokenGetRole_Success() {
        Integer role = 1;
        String token = JwtTokenUtil.generateToken(1001L, role);

        String parsedRole = JwtTokenUtil.parseTokenGetRole(token);

        assertEquals(String.valueOf(role), parsedRole);
    }

    @Test
    @DisplayName("测试4: Token包含正确的用户ID和角色")
    void testToken_ContainsCorrectClaims() {
        Long userId = 5678L;
        Integer role = 0;

        String token = JwtTokenUtil.generateToken(userId, role);
        String parsedUserId = JwtTokenUtil.parseTokenGetUserId(token);
        String parsedRole = JwtTokenUtil.parseTokenGetRole(token);

        assertEquals(String.valueOf(userId), parsedUserId);
        assertEquals(String.valueOf(role), parsedRole);
    }

    @Test
    @DisplayName("测试5: 不同用户生成不同Token")
    void testDifferentUsers_GenerateDifferentTokens() {
        String token1 = JwtTokenUtil.generateToken(1001L, 0);
        String token2 = JwtTokenUtil.generateToken(1002L, 0);

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("测试6: 相同用户生成不同Token(时间戳不同)")
    void testSameUser_GenerateDifferentTokens() throws InterruptedException {
        String token1 = JwtTokenUtil.generateToken(1001L, 0);
        Thread.sleep(10);
        String token2 = JwtTokenUtil.generateToken(1001L, 0);

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("测试7: 无效Token解析失败")
    void testInvalidToken_ParseFailure() {
        String invalidToken = "invalid.token.here";

        assertThrows(RuntimeException.class, () -> {
            JwtTokenUtil.parseTokenGetUserId(invalidToken);
        });
    }

    @Test
    @DisplayName("测试8: 空Token解析失败")
    void testNullToken_ParseFailure() {
        assertThrows(Exception.class, () -> {
            JwtTokenUtil.parseTokenGetUserId(null);
        });
    }

    @Test
    @DisplayName("测试9: 空字符串Token解析失败")
    void testEmptyToken_ParseFailure() {
        assertThrows(Exception.class, () -> {
            JwtTokenUtil.parseTokenGetUserId("");
        });
    }

    @Test
    @DisplayName("测试10: 截断的Token解析失败")
    void testTruncatedToken_ParseFailure() {
        String validToken = JwtTokenUtil.generateToken(1001L, 0);
        String truncatedToken = validToken.substring(0, validToken.length() - 10);

        assertThrows(RuntimeException.class, () -> {
            JwtTokenUtil.parseTokenGetUserId(truncatedToken);
        });
    }
}
