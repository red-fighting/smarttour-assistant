package com.panduoma.trevaljava.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.panduoma.trevaljava.entity.Product;
import com.panduoma.trevaljava.entity.User;
import com.panduoma.trevaljava.mapper.CartOrderMapper;
import com.panduoma.trevaljava.mapper.ProductMapper;
import com.panduoma.trevaljava.mapper.UserMapper;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员后台接口。
 * 所有接口都校验 Authorization token，且要求用户 role == 1，否则返回 403。
 */
@CrossOrigin
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CartOrderMapper cartOrderMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 根据 Authorization Header 拿到当前用户，若未登录/不是管理员则返回 null。
     */
    private User requireAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        Long userId;
        try {
            userId = jwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
        if (userId == null) return null;
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) return null;
        if (user.getRole() == null || user.getRole() != 1) return null;
        return user;
    }

    // ======================== 仪表盘/统计 ========================

    /**
     * 获取仪表盘数据：用户数 / 商品数 / 订单总数 / 已支付金额 / 未支付金额 / 按状态订单详情
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");

        List<User> users = userMapper.selectAllUsers();
        List<Product> products = productMapper.selectAll();
        List<Map<String, Object>> stats = cartOrderMapper.selectOrderStats();

        int cartCnt = 0, paidCnt = 0;
        BigDecimal paidAmount = BigDecimal.ZERO, cartAmount = BigDecimal.ZERO;
        for (Map<String, Object> s : stats) {
            Number status = (Number) s.get("status");
            Number cnt = (Number) s.getOrDefault("cnt", 0);
            Object amountObj = s.getOrDefault("amount", BigDecimal.ZERO);
            BigDecimal amt = amountObj instanceof BigDecimal ? (BigDecimal) amountObj
                    : new BigDecimal(String.valueOf(amountObj));
            if (status != null && status.intValue() == 1) {
                paidCnt = cnt.intValue();
                paidAmount = amt;
            } else {
                cartCnt += cnt.intValue();
                cartAmount = cartAmount.add(amt);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userCount", users.size());
        data.put("productCount", products.size());
        data.put("cartCount", cartCnt);
        data.put("paidCount", paidCnt);
        data.put("paidAmount", paidAmount);
        data.put("cartAmount", cartAmount);
        data.put("statusStats", stats);
        return Result.success(data);
    }

    // ======================== 用户管理 ========================

    @GetMapping("/users")
    public Result<List<User>> users(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");
        List<User> list = userMapper.selectAllUsers();
        // 清空密码，避免泄露
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    /**
     * 切换用户启用/停用状态：status 1=正常 0=禁用
     */
    @PostMapping("/user/toggleStatus")
    public Result<Void> toggleUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");
        Number id = (Number) body.get("id");
        if (id == null) return Result.fail(400, "缺少用户ID");
        User target = userMapper.selectByPrimaryKey(id.longValue());
        if (target == null) return Result.fail(404, "用户不存在");
        int nextStatus = (target.getStatus() == null || target.getStatus() == 1) ? 0 : 1;
        userMapper.updateUserStatus(id.longValue(), nextStatus);
        return Result.success(null);
    }

    /**
     * 设置用户角色：role 0=普通 1=管理员
     */
    @PostMapping("/user/setRole")
    public Result<Void> setUserRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");
        Number id = (Number) body.get("id");
        Number role = (Number) body.get("role");
        if (id == null || role == null) return Result.fail(400, "参数缺失");
        int r = role.intValue() == 1 ? 1 : 0;
        User user = new User();
        user.setId(id.longValue());
        user.setRole(r);
        userMapper.updateByPrimaryKeySelective(user);
        return Result.success(null);
    }

    // ======================== 商品管理 ========================

    @GetMapping("/products")
    public Result<List<Product>> products(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");
        return Result.success(productMapper.selectAll());
    }

    /**
     * 新增 / 更新商品。若 id 为空或 0 则新增，否则更新。
     */
    @PostMapping("/product/save")
    public Result<Product> saveProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");

        Product p = new Product();
        Number idNum = (Number) body.get("id");
        if (idNum != null && idNum.longValue() > 0) {
            p.setId(idNum.longValue());
        }
        p.setTitle((String) body.get("title"));
        p.setOrigin((String) body.get("origin"));
        p.setDestination((String) body.get("destination"));

        Object duration = body.get("duration");
        p.setDuration(duration == null ? null : String.valueOf(duration));

        Object price = body.get("price");
        p.setPrice(price instanceof BigDecimal ? (BigDecimal) price : new BigDecimal(String.valueOf(price)));

        p.setImage((String) body.get("image"));

        Object imagesObj = body.get("images");
        if (imagesObj == null) {
            p.setImages("[]");
        } else if (imagesObj instanceof String) {
            p.setImages((String) imagesObj);
        } else {
            try {
                p.setImages(OBJECT_MAPPER.writeValueAsString(imagesObj));
            } catch (Exception e) {
                p.setImages("[]");
            }
        }

        p.setRouteDesc((String) body.get("routeDesc"));
        p.setFoodFeature((String) body.get("foodFeature"));
        p.setGuideName((String) body.get("guideName"));
        p.setGuidePhone((String) body.get("guidePhone"));

        Number soldNum = (Number) body.get("soldCount");
        p.setSoldCount(soldNum == null ? 0 : soldNum.intValue());

        if (p.getId() == null) {
            if (p.getTitle() == null || p.getTitle().isBlank()) {
                return Result.fail(400, "标题不能为空");
            }
            productMapper.insert(p);
        } else {
            int rows = productMapper.update(p);
            if (rows <= 0) return Result.fail(404, "商品不存在");
        }
        return Result.success(p);
    }

    /**
     * 删除商品
     */
    @PostMapping("/product/delete")
    public Result<Void> deleteProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");
        Number id = (Number) body.get("id");
        if (id == null) return Result.fail(400, "缺少商品ID");
        int rows = productMapper.deleteById(id.longValue());
        if (rows <= 0) return Result.fail(404, "商品不存在");
        return Result.success(null);
    }

    // ======================== 订单管理 ========================

    @GetMapping("/orders")
    public Result<List<Map<String, Object>>> orders(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "status", required = false, defaultValue = "-1") Integer status) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");
        return Result.success(cartOrderMapper.selectAllOrders(status));
    }

    @PostMapping("/order/updateStatus")
    public Result<Void> updateOrderStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");
        Number id = (Number) body.get("id");
        Number status = (Number) body.get("status");
        if (id == null || status == null) return Result.fail(400, "参数缺失");
        int s = status.intValue() == 1 ? 1 : 0;
        int rows = cartOrderMapper.updateOrderStatus(id.longValue(), s);
        if (rows <= 0) return Result.fail(404, "订单不存在");
        return Result.success(null);
    }

    @PostMapping("/order/delete")
    public Result<Void> deleteOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        User admin = requireAdmin(authHeader);
        if (admin == null) return Result.fail(403, "无权访问");
        Number id = (Number) body.get("id");
        if (id == null) return Result.fail(400, "参数缺失");
        int rows = cartOrderMapper.deleteOrderById(id.longValue());
        if (rows <= 0) return Result.fail(404, "订单不存在");
        return Result.success(null);
    }
}
