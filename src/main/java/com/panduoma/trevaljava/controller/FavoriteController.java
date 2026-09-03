package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.entity.Favorite;
import com.panduoma.trevaljava.service.FavoriteService;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
@CrossOrigin
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private JwtUtils jwtUtils;

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtils.getUserIdFromToken(token);
    }

    /** 添加收藏 */
    @PostMapping("/add")
    public Result<Void> addFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return favoriteService.addFavorite(userId, body);
    }

    /** 取消收藏 */
    @PostMapping("/remove")
    public Result<Void> removeFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Long> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return favoriteService.removeFavorite(userId, body.get("postId"));
    }

    /** 获取我的收藏列表 */
    @GetMapping("/list")
    public Result<List<Favorite>> getFavorites(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return favoriteService.getFavorites(userId);
    }

    /** 检查是否已收藏 */
    @GetMapping("/check")
    public Result<Map<String, Object>> isFavorited(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam Long postId
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return favoriteService.isFavorited(userId, postId);
    }

    /** 获取收藏数量 */
    @GetMapping("/count")
    public Result<Map<String, Object>> getFavoriteCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return favoriteService.getFavoriteCount(userId);
    }
}
