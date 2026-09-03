package com.panduoma.trevaljava.service;

import com.panduoma.trevaljava.entity.Product;
import com.panduoma.trevaljava.mapper.ProductMapper;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /** 获取所有商品（按销量排序） */
    public Result<List<Product>> getAllProducts() {
        return Result.success(productMapper.selectAll());
    }

    /** 获取单个商品详情 */
    public Result<Product> getProductDetail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) return Result.fail(404, "商品不存在");
        return Result.success(p);
    }

    /** 动态条件查询商品（起始地/目的地/预算/天数 全部可选） */
    public Result<List<Product>> searchProducts(String origin, String destination,
                                                Double budget, Integer duration) {
        return Result.success(productMapper.searchProducts(origin, destination, budget, duration));
    }
}
