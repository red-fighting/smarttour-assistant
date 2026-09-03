package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.entity.Banner;
import com.panduoma.trevaljava.entity.User;
import com.panduoma.trevaljava.mapper.BannerMapper;
import com.panduoma.trevaljava.mapper.UserMapper;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 首页轮播图接口。
 * - GET  /api/banner/list        公开：首页轮播数据（仅启用，按 sort_order 排序）
 * - GET  /api/banner/admin/list  管理员：全部轮播图（含禁用）
 * - POST /api/banner/admin/add    管理员：新增
 * - POST /api/banner/admin/update 管理员：修改（id 必传）
 * - POST /api/banner/admin/delete 管理员：删除（id 必传）
 */
@CrossOrigin
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerMapper bannerMapper;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;

    /** 与 AdminController 相同的管理员校验：未登录/非管理员返回 null */
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
        if (user == null || user.getRole() == null || user.getRole() != 1) return null;
        return user;
    }

    /** 公开接口：首页轮播 */
    @GetMapping("/list")
    public Result<List<Banner>> list() {
        return Result.success(bannerMapper.selectEnabled());
    }

    // ==================== 管理员接口 ====================

    @GetMapping("/admin/list")
    public Result<List<Banner>> adminList(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (requireAdmin(authHeader) == null) return Result.fail(403, "无权访问");
        return Result.success(bannerMapper.selectAll());
    }

    @PostMapping("/admin/add")
    public Result<Banner> add(@RequestHeader(value = "Authorization", required = false) String authHeader,
                              @RequestBody Banner banner) {
        if (requireAdmin(authHeader) == null) return Result.fail(403, "无权访问");
        if (banner.getImageUrl() == null || banner.getImageUrl().isBlank()) {
            return Result.fail(400, "图片 URL 不能为空");
        }
        if (banner.getTitle() == null) banner.setTitle("");
        if (banner.getSubtitle() == null) banner.setSubtitle("");
        if (banner.getSortOrder() == null) banner.setSortOrder(0);
        if (banner.getStatus() == null) banner.setStatus(1);
        bannerMapper.insert(banner);
        return Result.success(banner);
    }

    @PostMapping("/admin/update")
    public Result<String> update(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                 @RequestBody Banner banner) {
        if (requireAdmin(authHeader) == null) return Result.fail(403, "无权访问");
        if (banner.getId() == null) return Result.fail(400, "缺少 id");
        if (bannerMapper.selectById(banner.getId()) == null) return Result.fail(404, "轮播图不存在");
        if (banner.getImageUrl() == null || banner.getImageUrl().isBlank()) {
            return Result.fail(400, "图片 URL 不能为空");
        }
        bannerMapper.update(banner);
        return Result.success("更新成功");
    }

    @PostMapping("/admin/delete")
    public Result<String> delete(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                 @RequestBody Map<String, Object> body) {
        if (requireAdmin(authHeader) == null) return Result.fail(403, "无权访问");
        Object idObj = body.get("id");
        if (idObj == null) return Result.fail(400, "缺少 id");
        Long id = Long.valueOf(String.valueOf(idObj));
        if (bannerMapper.selectById(id) == null) return Result.fail(404, "轮播图不存在");
        bannerMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
