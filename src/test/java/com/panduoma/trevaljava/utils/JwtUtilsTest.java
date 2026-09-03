package com.panduoma.trevaljava.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils 单元测试。
 * 纯单元测试：不启动 Spring 容器、不依赖数据库，通过 ReflectionTestUtils 注入 secret/expiration。
 * 对应测试用例文档：docs/测试用例.md TC-USER-012（JWT 生成/解析/校验）。
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 与 application.yml 中 jwt.secret 保持一致（至少 32 字符，HS256 要求）
        ReflectionTestUtils.setField(jwtUtils, "secret",
                "your-256-bit-secret-key-must-be-at-least-32-characters");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 86400000L); // 24 小时
    }

    @Test
    @DisplayName("生成 Token 后可正常解析出 userId 和 username")
    void generateToken_thenParse_shouldReturnSameClaims() {
        String token = jwtUtils.generateToken(1001L, "testfav_a");

        assertNotNull(token, "生成的 token 不应为 null");
        assertTrue(token.split("\\.").length == 3, "JWT 应由三段（header.payload.signature）组成");
        assertEquals(1001L, jwtUtils.getUserIdFromToken(token), "解析出的 userId 应与签发时一致");
        assertEquals("testfav_a", jwtUtils.getUsernameFromToken(token), "解析出的 username 应与签发时一致");
    }

    @Test
    @DisplayName("合法 Token 校验应返回 true")
    void validateToken_validToken_shouldReturnTrue() {
        String token = jwtUtils.generateToken(1L, "admin");
        assertTrue(jwtUtils.validateToken(token), "合法 token 校验应通过");
    }

    @Test
    @DisplayName("伪造/篡改 Token 校验应返回 false 而不是抛异常")
    void validateToken_invalidToken_shouldReturnFalse() {
        assertFalse(jwtUtils.validateToken("invalid.token.here"), "非法 token 应返回 false");
        assertFalse(jwtUtils.validateToken(""), "空字符串 token 应返回 false");
        assertFalse(jwtUtils.validateToken(null), "null token 应返回 false");
    }

    @Test
    @DisplayName("被篡改签名的 Token 应校验失败")
    void validateToken_tamperedToken_shouldReturnFalse() {
        String token = jwtUtils.generateToken(1L, "admin");
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertFalse(jwtUtils.validateToken(tampered), "签名被篡改的 token 应校验失败");
    }
}
