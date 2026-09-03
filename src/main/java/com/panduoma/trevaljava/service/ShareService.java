package com.panduoma.trevaljava.service;

import com.panduoma.trevaljava.entity.Share;
import com.panduoma.trevaljava.mapper.ShareLikeMapper;
import com.panduoma.trevaljava.mapper.ShareMapper;
import com.panduoma.trevaljava.mapper.CommentMapper;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShareService {

    @Autowired
    private ShareMapper shareMapper;
    @Autowired
    private ShareLikeMapper shareLikeMapper;
    @Autowired
    private CommentMapper commentMapper;

    /** 发布分享 */
    public Result<Map<String, Object>> createShare(Long userId, Map<String, Object> body) {
        Share share = new Share();
        share.setUserId(userId);
        share.setTitle((String) body.get("title"));
        share.setContent((String) body.get("content"));
        share.setImages(body.get("images") != null ? body.get("images").toString() : null);
        share.setTag((String) body.get("tag"));
        share.setLikes(0);
        share.setComments(0);
        shareMapper.insert(share);

        Map<String, Object> data = new HashMap<>();
        data.put("id", share.getId());
        return Result.success(data);
    }

    /** 删除分享（同时清理关联的评论记录） */
    @Transactional
    public Result<Void> deleteShare(Long userId, Long shareId) {
        commentMapper.deleteByShareId(shareId);
        shareMapper.deleteByIdAndUserId(shareId, userId);
        return Result.success(null);
    }

    /** 获取我的分享列表 */
    public Result<List<Share>> getMyShares(Long userId) {
        List<Share> list = shareMapper.selectByUserId(userId);
        return Result.success(list);
    }

    /** 获取所有分享（社区广场，JOIN user 拿发帖人信息） */
    public Result<List<Map<String, Object>>> getAllShares() {
        List<Map<String, Object>> list = shareMapper.selectAllWithUser();
        return Result.success(list);
    }

    /**
     * 点赞/取消点赞（用户隔离 + 全局计数）
     * @param userId 操作的用户
     * @param shareId 分享ID
     * @param liked 1=点赞 0=取消
     * @return 最新 likes 数 + 当前用户是否已点赞
     */
    @Transactional
    public Result<Map<String, Object>> likeShare(Long userId, Long shareId, int liked) {
        Share share = shareMapper.selectAll().stream()
                .filter(s -> s.getId().equals(shareId))
                .findFirst()
                .orElse(null);
        if (share == null) {
            return Result.fail(404, "分享不存在");
        }

        boolean nowLiked;
        int newLikes;

        if (liked == 1) {
            // 点赞：如果还没点过才插入（防止重复点赞）
            int existing = shareLikeMapper.countByShareAndUser(shareId, userId);
            if (existing == 0) {
                shareLikeMapper.insertLike(shareId, userId);
                newLikes = share.getLikes() + 1;
            } else {
                // 已经点过了，直接返回当前状态
                newLikes = share.getLikes();
            }
            nowLiked = true;
        } else {
            // 取消点赞
            shareLikeMapper.deleteLike(shareId, userId);
            newLikes = Math.max(0, share.getLikes() - 1);
            nowLiked = false;
        }

        shareMapper.updateInteractions(shareId, newLikes, share.getComments());

        Map<String, Object> data = new HashMap<>();
        data.put("likes", newLikes);
        data.put("liked", nowLiked);
        return Result.success(data);
    }

    /** 评论数+1 */
    public Result<Void> incrementComment(Long shareId) {
        List<Share> all = shareMapper.selectAll();
        Share share = all.stream()
                .filter(s -> s.getId().equals(shareId))
                .findFirst()
                .orElse(null);
        if (share == null) {
            return Result.fail(404, "分享不存在");
        }
        shareMapper.updateInteractions(shareId, share.getLikes(), share.getComments() + 1);
        return Result.success(null);
    }

    /** 获取我的分享数量 */
    public Result<Map<String, Object>> getShareCount(Long userId) {
        int count = shareMapper.countByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    /** 查当前用户点赞过的所有 shareId */
    public Set<Long> getLikedShareIds(Long userId) {
        if (userId == null) return new HashSet<>();
        List<Long> ids = shareLikeMapper.selectShareIdsByUserId(userId);
        return ids == null ? new HashSet<>() : new HashSet<>(ids);
    }
}
