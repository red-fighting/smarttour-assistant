package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.entity.Favorite;
import com.panduoma.trevaljava.entity.Share;
import com.panduoma.trevaljava.mapper.FavoriteMapper;
import com.panduoma.trevaljava.service.ShareService;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/share")
@CrossOrigin
public class ShareController {

    @Autowired
    private ShareService shareService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private FavoriteMapper favoriteMapper;

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtils.getUserIdFromToken(token);
    }

    /** 把 Share 实体转为 Map 并附上当前用户的 liked / favorited 状态 */
    private Map<String, Object> shareToMap(Share share, Long currentUserId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", share.getId());
        map.put("userId", share.getUserId());
        map.put("title", share.getTitle());
        map.put("content", share.getContent());
        map.put("images", share.getImages());
        map.put("tag", share.getTag());
        map.put("likes", share.getLikes());
        map.put("comments", share.getComments());
        map.put("createTime", share.getCreateTime());
        // 动态状态字段（当前用户视角）
        map.put("liked", false);
        map.put("favorited", false);
        return map;
    }

    /** 批量填充 liked / favorited 状态 */
    private void fillUserStates(List<Map<String, Object>> shareMaps, Long currentUserId) {
        if (currentUserId == null) return;

        // 1. 查当前用户所有收藏的 postId
        List<Favorite> favs = favoriteMapper.selectByUserId(currentUserId);
        Set<Long> favPostIds = favs.stream()
                .map(Favorite::getPostId)
                .collect(Collectors.toSet());

        // 2. 查当前用户所有点赞的 shareId（用一条 SQL）
        //    这里直接在内存查 share_like 表 —— 用 JdbcTemplate 或加一个 Mapper 方法
        Set<Long> likedShareIds = shareService.getLikedShareIds(currentUserId);

        for (Map<String, Object> m : shareMaps) {
            Long id = ((Number) m.get("id")).longValue();
            m.put("liked", likedShareIds.contains(id));
            m.put("favorited", favPostIds.contains(id));
        }
    }

    // ==================== 业务接口 ====================

    /** 发布分享 */
    @PostMapping("/create")
    public Result<Map<String, Object>> createShare(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return shareService.createShare(userId, body);
    }

    /** 删除分享 */
    @PostMapping("/delete")
    public Result<Void> deleteShare(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Long> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return shareService.deleteShare(userId, body.get("id"));
    }

    /** 获取我的分享（带当前用户状态） */
    @GetMapping("/mine")
    public Result<List<Map<String, Object>>> getMyShares(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");

        Result<List<Share>> res = shareService.getMyShares(userId);
        List<Map<String, Object>> maps = res.getData().stream()
                .map(s -> shareToMap(s, userId))
                .collect(Collectors.toList());
        fillUserStates(maps, userId);
        return Result.success(maps);
    }

    /** 获取所有分享（社区广场，可匿名访问，带当前用户状态 + 发帖人信息） */
    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllShares(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long currentUserId = getUserId(authHeader); // 可能为 null（匿名访问）

        Result<List<Map<String, Object>>> res = shareService.getAllShares();
        List<Map<String, Object>> maps = res.getData();

        // 给每条帖子加动态状态字段
        for (Map<String, Object> m : maps) {
            m.put("liked", false);
            m.put("favorited", false);
        }
        fillUserStates(maps, currentUserId);
        return Result.success(maps);
    }

    /**
     * 点赞/取消点赞（用户隔离 + 持久化）
     * 请求体: { shareId: Long, liked: 1|0 }
     * 返回: { likes: 最新数量, liked: 当前是否已点赞 }
     */
    @PostMapping("/like")
    public Result<Map<String, Object>> likeShare(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Integer> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");

        Integer shareId = body.get("shareId");
        Integer liked = body.get("liked");
        if (shareId == null || liked == null) {
            return Result.fail(400, "参数错误");
        }
        return shareService.likeShare(userId, shareId.longValue(), liked);
    }

    /** 评论数+1（已废弃，用 CommentController /create 替代，保留兼容） */
    @PostMapping("/comment")
    public Result<Void> incrementComment(
            @RequestBody Map<String, Long> body
    ) {
        return shareService.incrementComment(body.get("shareId"));
    }

    /** 获取我的分享数量 */
    @GetMapping("/count")
    public Result<Map<String, Object>> getShareCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        return shareService.getShareCount(userId);
    }
}
