package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.entity.CartOrder;
import com.panduoma.trevaljava.service.CartOrderService;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartOrderController {

    @Autowired
    private CartOrderService cartOrderService;

    @Autowired
    private JwtUtils jwtUtils;

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return jwtUtils.getUserIdFromToken(authHeader.substring(7));
    }

    /** 添加到购物车 body: {productId, quantity?, startDate, endDate} */
    @PostMapping("/add")
    public Result<Map<String, Object>> addToCart(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        Long productId = Long.valueOf(body.get("productId").toString());
        int qty = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 1;
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (body.get("startDate") != null) {
            startDate = LocalDate.parse(body.get("startDate").toString());
        }
        if (body.get("endDate") != null) {
            endDate = LocalDate.parse(body.get("endDate").toString());
        }
        return cartOrderService.addToCart(userId, productId, qty, startDate, endDate);
    }

    /** 获取我的订单/购物车 status: 0=购物车 1=已支付 不传/传-1=全部 */
    @GetMapping("/my")
    public Result<List<CartOrder>> getMyOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return cartOrderService.getMyOrders(userId, status);
    }

    /** 删除一条订单/购物车记录 body: {id} */
    @PostMapping("/delete")
    public Result<Void> deleteOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Long> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return cartOrderService.deleteOrder(userId, body.get("id"));
    }

    /** 支付一条记录 body: {id} */
    @PostMapping("/pay")
    public Result<Void> payOne(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Long> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return cartOrderService.payOne(userId, body.get("id"));
    }

    /** 一键支付全部购物车 */
    @PostMapping("/payAll")
    public Result<Map<String, Object>> payAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return cartOrderService.payAll(userId);
    }

    /** 获取数量统计 */
    @GetMapping("/counts")
    public Result<Map<String, Object>> getCounts(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return cartOrderService.getCounts(userId);
    }
}
