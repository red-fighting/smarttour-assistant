package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.vo.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 购物车 / 商品接口集成测试。
 *
 * 直接注入 Controller 调用其方法（不依赖 MockMvc，避免 web 测试层依赖缺失）。
 *
 * 运行前提：
 *  1. MySQL 已启动且 travel 库可连接（application.yml 配置）；
 *  2. product 表存在（product_cart_tables.sql），cart_order 表含 start_date/end_date 列；
 *  3. 类上 @Transactional 保证写库用例结束自动回滚，不污染数据库。
 *
 * 对应测试用例文档：docs/测试用例.md
 *  TC-CART-001（未登录 401）、TC-CART-009（缺日期 400）、TC-CART-006（完整加购 200）、
 *  TC-PROD-001（商品列表）、TC-PROD-003（动态查询）、TC-CART-010（未登录查购物车 401）。
 *
 * 说明：项目 Result.fail(401,...) 为 HTTP 200、响应体 code=401，故断言 result.getCode()。
 */
@SpringBootTest
@Transactional
class CartOrderControllerTest {

    @Autowired
    private CartOrderController cartOrderController;

    @Autowired
    private ProductController productController;

    @Autowired
    private JwtUtils jwtUtils;

    private String authHeader(Long userId, String username) {
        return "Bearer " + jwtUtils.generateToken(userId, username);
    }

    private Map<String, Object> body(Object... kv) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(kv[i].toString(), kv[i + 1]);
        }
        return map;
    }

    @Test
    @DisplayName("TC-CART-001 未登录加购应返回 code=401")
    void addToCart_withoutToken_shouldReturn401() {
        Result<?> result = cartOrderController.addToCart(null,
                body("productId", 1, "quantity", 1,
                        "startDate", "2026-09-05", "endDate", "2026-09-07"));
        assertEquals(401, result.getCode());
    }

    @Test
    @DisplayName("TC-CART-009 已登录但缺少出行日期应返回 code=400")
    void addToCart_withoutDates_shouldReturn400() {
        Result<?> result = cartOrderController.addToCart(
                authHeader(999999L, "case_test_user"),
                body("productId", 1, "quantity", 1));
        assertEquals(400, result.getCode());
    }

    @Test
    @DisplayName("TC-CART-006 已登录且日期完整，加购应成功 code=200（事务回滚不落库）")
    void addToCart_withValidDates_shouldReturn200() {
        Result<?> result = cartOrderController.addToCart(
                authHeader(999999L, "case_test_user"),
                body("productId", 1, "quantity", 1,
                        "startDate", "2026-09-05", "endDate", "2026-09-07"));
        assertEquals(200, result.getCode(), "加购失败信息：" + result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("TC-PROD-001 商品公开列表 code=200")
    void productList_shouldReturn200() {
        Result<?> result = productController.list();
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("TC-PROD-003 商品动态查询（起始地/目的地/预算/天数）code=200")
    void productSearch_withFilters_shouldReturn200() {
        Result<?> result = productController.search("成都", "九寨沟", 5000.0, 3);
        assertEquals(200, result.getCode());
    }

    @Test
    @DisplayName("TC-CART-010 未登录查询购物车应返回 code=401")
    void myCart_withoutToken_shouldReturn401() {
        Result<?> result = cartOrderController.getMyOrders(null, 0);
        assertEquals(401, result.getCode());
    }
}
