package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.entity.Product;
import com.panduoma.trevaljava.service.ProductService;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    /** 获取所有商品列表（公开，无需登录） */
    @GetMapping("/list")
    public Result<List<Product>> list() {
        return productService.getAllProducts();
    }

    /** 获取单个商品详情（公开，无需登录） */
    @GetMapping("/detail")
    public Result<Product> detail(@RequestParam Long id) {
        return productService.getProductDetail(id);
    }

    /** 动态条件查询商品（公开，无需登录）
     * 参数全可选：origin 起始地, destination 目的地, budget 预算上限, duration 天数 */
    @GetMapping("/search")
    public Result<List<Product>> search(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Double budget,
            @RequestParam(required = false) Integer duration) {
        return productService.searchProducts(origin, destination, budget, duration);
    }
}
