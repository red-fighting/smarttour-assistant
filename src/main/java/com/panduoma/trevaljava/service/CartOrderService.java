package com.panduoma.trevaljava.service;

import com.panduoma.trevaljava.entity.CartOrder;
import com.panduoma.trevaljava.entity.Product;
import com.panduoma.trevaljava.mapper.CartOrderMapper;
import com.panduoma.trevaljava.mapper.ProductMapper;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartOrderService {

    @Autowired
    private CartOrderMapper cartOrderMapper;

    @Autowired
    private ProductMapper productMapper;

    /** 添加到购物车（重复加入只增加数量，同商品+同日期才合并） */
    public Result<Map<String, Object>> addToCart(Long userId, Long productId, int quantity,
                                                  LocalDate startDate, LocalDate endDate) {
        if (productId == null || quantity <= 0) {
            return Result.fail(400, "参数错误");
        }
        if (startDate == null || endDate == null) {
            return Result.fail(400, "请选择出发日期和返回日期");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) return Result.fail(404, "商品不存在");

        // 同商品 + 同日期 才合并数量
        CartOrder existing = cartOrderMapper.selectCartExisting(userId, productId, startDate, endDate);
        if (existing != null) {
            cartOrderMapper.incQuantity(existing.getId(), quantity);
            Map<String, Object> data = new HashMap<>();
            data.put("id", existing.getId());
            data.put("type", "inc");
            data.put("quantity", existing.getQuantity() + quantity);
            return Result.success(data);
        }

        CartOrder cart = new CartOrder();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setPrice(product.getPrice()); // 价格快照
        cart.setTitleSnapshot(product.getTitle());
        cart.setImageSnapshot(product.getImage());
        cart.setStatus(0); // 购物车态
        cart.setStartDate(startDate);
        cart.setEndDate(endDate);
        cartOrderMapper.insert(cart);

        Map<String, Object> data = new HashMap<>();
        data.put("id", cart.getId());
        data.put("type", "new");
        data.put("quantity", quantity);
        return Result.success(data);
    }

    /** 获取用户的订单/购物车列表 status: 0=购物车 1=已支付 -1=全部 */
    public Result<List<CartOrder>> getMyOrders(Long userId, Integer status) {
        if (status == null) status = -1;
        List<CartOrder> list = cartOrderMapper.selectByUserIdAndStatus(userId, status);
        return Result.success(list);
    }

    /** 删除一条 */
    public Result<Void> deleteOrder(Long userId, Long id) {
        cartOrderMapper.deleteByIdAndUserId(id, userId);
        return Result.success(null);
    }

    /** 支付一条 */
    public Result<Void> payOne(Long userId, Long id) {
        // 简单处理：直接 payById
        int rows = cartOrderMapper.payById(id, userId);
        if (rows <= 0) return Result.fail(400, "支付失败（未找到购物车记录）");
        // 查 product 增加销量
        List<CartOrder> updated = cartOrderMapper.selectByUserIdAndStatus(userId, -1);
        CartOrder paid = updated.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
        if (paid != null && paid.getProductId() != null) {
            productMapper.incSoldCount(paid.getProductId(), paid.getQuantity());
        }
        return Result.success(null);
    }

    /** 一键支付用户所有购物车 */
    public Result<Map<String, Object>> payAll(Long userId) {
        int count = cartOrderMapper.countByUserIdAndStatus(userId, 0);
        if (count == 0) return Result.fail(400, "购物车是空的");
        cartOrderMapper.payAllByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        // 总金额
        List<CartOrder> paidList = cartOrderMapper.selectByUserIdAndStatus(userId, 1); // 已支付的
        BigDecimal total = BigDecimal.ZERO;
        int paidCount = 0;
        for (CartOrder co : paidList) {
            if (co.getPaidTime() == null) continue;
            total = total.add(co.getPrice().multiply(BigDecimal.valueOf(co.getQuantity())));
            paidCount++;
            productMapper.incSoldCount(co.getProductId(), co.getQuantity());
            if (paidCount >= count) break;
        }
        data.put("totalAmount", total);
        return Result.success(data);
    }

    /** 获取数量统计：0=购物车，1=已支付 */
    public Result<Map<String, Object>> getCounts(Long userId) {
        int cart = cartOrderMapper.countByUserIdAndStatus(userId, 0);
        int paid = cartOrderMapper.countByUserIdAndStatus(userId, 1);
        Map<String, Object> data = new HashMap<>();
        data.put("cartCount", cart);
        data.put("paidCount", paid);
        return Result.success(data);
    }
}
