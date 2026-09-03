package com.panduoma.trevaljava.service;

import com.panduoma.trevaljava.entity.Comment;
import com.panduoma.trevaljava.entity.User;
import com.panduoma.trevaljava.mapper.CommentMapper;
import com.panduoma.trevaljava.mapper.ShareMapper;
import com.panduoma.trevaljava.mapper.UserMapper;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShareMapper shareMapper;

    /** 获取某条分享的所有评论 */
    public Result<List<Comment>> getByShareId(Long shareId) {
        List<Comment> list = commentMapper.selectByShareId(shareId);
        return Result.success(list);
    }

    /** 发表评论 */
    public Result<Map<String, Object>> createComment(Long userId, Long shareId, String text) {
        if (text == null || text.trim().isEmpty()) {
            return Result.fail(400, "评论内容不能为空");
        }
        if (shareId == null) {
            return Result.fail(400, "分享ID无效");
        }
        // 查用户信息（冗余保存 username/avatar）
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        Comment comment = new Comment();
        comment.setShareId(shareId);
        comment.setUserId(userId);
        comment.setUsername(user.getUsername());
        comment.setAvatar(user.getAvatar());
        comment.setText(text.trim());
        commentMapper.insert(comment);

        // 评论数 +1（更新 share 表的 comments 计数）
        shareMapper.selectAll().stream()
                .filter(s -> s.getId().equals(shareId))
                .findFirst()
                .ifPresent(share -> {
                    shareMapper.updateInteractions(shareId, share.getLikes(), share.getComments() + 1);
                });

        Map<String, Object> data = new HashMap<>();
        data.put("id", comment.getId());
        return Result.success(data);
    }
}
