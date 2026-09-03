package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.dto.LoginRequestDTO;

import com.panduoma.trevaljava.entity.User;
import com.panduoma.trevaljava.service.UserService;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@CrossOrigin
@RestController     //标记当前类为接口控制器，自动返回 JSON 数据，不用额外加 @ResponseBody
@RequestMapping("/api/user")        //类级别统一路由前缀,当前类所有接口地址都会拼接 /api/travel。
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "avatar", required = false) String avatar,
            @RequestParam(value = "role", required = false, defaultValue = "0") Integer role,
            @RequestParam(value = "registerCode", required = false) String registerCode
    ) throws IOException{
        return userService.register(username, password, confirmPassword, phone, email, avatar, role, registerCode);
    }

    /** 根据用户名获取头像（登录页预览用） */
    @GetMapping("/avatar")
    public Result<Map<String, Object>> getAvatarByUsername(@RequestParam("username") String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("avatar", user.getAvatar());
        data.put("username", user.getUsername());
        return Result.success(data);
    }
    //@GetMapping：查询数据，参数放 URL 拼接，不能用 @RequestBody
    //@PostMapping：提交 / 新增，参数放请求体 JSON，搭配 @RequestBody 接收前端 JSON
    //@Valid:参数校验注解,配合 DTO 实体类上的校验注解（@NotBlank、@NotNull、@Size），自动校验前端传参是否合法：
    //@RequestBody:把前端POST 请求里的 JSON 请求体，自动转换成后端 Java 对象（DTO / 实体类）
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO dto) {
        return userService.login(dto);

    }
    @GetMapping("/userInfo")
    public Result<Map<String, Object>> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // 1. 解析 token 获取 userId
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未登录");
        }
        String token = authHeader.substring(7);
        Long userId;
        try {
            userId = jwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            return Result.fail(401, "Token无效");
        }

        // 2. 查询用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        // 3. 返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("avatar", user.getAvatar());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("role", user.getRole() == null ? 0 : user.getRole());
        data.put("status", user.getStatus() == null ? 1 : user.getStatus());

        return Result.success(data);
    }

    // ========== 修改密码 ==========
    @PostMapping("/changePassword")
    public Result<Void> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未登录");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(token);

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    // ========== 更新个人资料 ==========
    @PostMapping("/updateProfile")
    public Result<Void> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未登录");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(token);

        User user = new User();
        user.setId(userId);
        user.setUsername((String) body.get("username"));
        user.setEmail((String) body.get("email"));
        user.setPhone((String) body.get("phone"));
        if (body.get("avatar") != null) {
            user.setAvatar((String) body.get("avatar"));
        }
        return userService.updateProfile(user);
    }

    // ========== 提交反馈 ==========
    @PostMapping("/feedback")
    public Result<Void> submitFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail(401, "未登录");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtils.getUserIdFromToken(token);

        String type = body.get("type");
        String content = body.get("content");
        String contact = body.get("contact");
        return userService.submitFeedback(userId, type, content, contact);
    }

}
