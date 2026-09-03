package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.entity.Comment;
import com.panduoma.trevaljava.service.CommentService;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private JwtUtils jwtUtils;

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtils.getUserIdFromToken(token);
    }

    /** 获取某条分享的评论列表 */
    @GetMapping("/list")
    public Result<List<Comment>> getComments(@RequestParam Long shareId) {
        return commentService.getByShareId(shareId);
    }

    /** 发表评论 */
    @PostMapping("/create")
    public Result<Map<String, Object>> createComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = getUserId(authHeader);
        if (userId == null) return Result.fail(401, "未登录");
        Long shareId = body.get("shareId") == null ? null : ((Number) body.get("shareId")).longValue();
        String text = (String) body.get("text");
        return commentService.createComment(userId, shareId, text);
    }
}
