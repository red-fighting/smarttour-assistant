package com.panduoma.trevaljava.service;

import com.panduoma.trevaljava.entity.Favorite;
import com.panduoma.trevaljava.mapper.FavoriteMapper;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private UserService userService;

    /** 添加收藏 */
    public Result<Void> addFavorite(Long userId, Map<String, Object> body) {
        Long postId = Long.valueOf(body.get("postId").toString());
        Favorite existing = favoriteMapper.selectByUserIdAndPostId(userId, postId);
        if (existing != null) {
            return Result.fail(400, "已收藏该内容");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setPostId(postId);
        fav.setPostTitle((String) body.get("title"));
        fav.setPostContent((String) body.get("content"));
        fav.setPostImages(body.get("images") != null ? body.get("images").toString() : null);
        fav.setPostTag((String) body.get("tag"));
        fav.setPostUsername((String) body.get("username"));
        fav.setPostAvatar((String) body.get("avatar"));
        fav.setPostLocation((String) body.get("location"));
        favoriteMapper.insert(fav);
        return Result.success(null);
    }

    /** 取消收藏 */
    public Result<Void> removeFavorite(Long userId, Long postId) {
        favoriteMapper.deleteByPostIdAndUserId(postId, userId);
        return Result.success(null);
    }

    /** 获取用户收藏列表 */
    public Result<List<Favorite>> getFavorites(Long userId) {
        List<Favorite> list = favoriteMapper.selectByUserId(userId);
        return Result.success(list);
    }

    /** 检查是否已收藏 */
    public Result<Map<String, Object>> isFavorited(Long userId, Long postId) {
        Favorite fav = favoriteMapper.selectByUserIdAndPostId(userId, postId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", fav != null);
        return Result.success(data);
    }

    /** 获取收藏数量 */
    public Result<Map<String, Object>> getFavoriteCount(Long userId) {
        int count = favoriteMapper.countByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }
}
