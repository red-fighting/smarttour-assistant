package com.panduoma.trevaljava.service;

import com.panduoma.trevaljava.dto.LoginRequestDTO;
import com.panduoma.trevaljava.entity.User;
import com.panduoma.trevaljava.mapper.UserMapper;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.utils.PasswordEncoder;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    @Autowired // 自动从容器拿 UserMapper 对象赋值给这个变量
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.admin.register-code}")
    private String adminRegisterCode;

    // 登录d
    public Result<Map<String, Object>> login(LoginRequestDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        // 判断用户名是否存在
        if (user == null) {
            System.out.println("❌ 用户不存在");
            return Result.fail(400, "该用户不存在,请先注册");
        }
        if (user.getStatus() == 0) {
            System.out.println("账户已被封禁");
            return Result.fail(400, "账户已被封禁");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.fail(400, "用户名或密码错误");
        }
        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole() == null ? 0 : user.getRole());
        data.put("status", user.getStatus() == null ? 1 : user.getStatus());
        return Result.success(data);
    }

    // 注册
    public Result<Map<String, Object>> register(String username,
            String password,
            String confirmPassword,
            String phone,
            String email,
            String avatar,
            Integer role,
            String registerCode) throws IOException {
        // 检查密码和确认密码
        if (!password.equals(confirmPassword)) {
            return Result.fail(400, "两次输入密码不一致");
        }

        // 检查用户名是否已存在（唯一约束）
        User existing = userMapper.selectByUsername(username);
        if (existing != null) {
            return Result.fail(400, "用户名已被注册，请更换用户名");
        }

        // 管理员注册码校验
        int finalRole = (role != null && role == 1) ? 1 : 0;
        if (finalRole == 1) {
            if (registerCode == null || registerCode.isBlank()) {
                return Result.fail(400, "注册管理员需要填写管理员注册码");
            }
            if (!registerCode.equals(adminRegisterCode)) {
                return Result.fail(400, "管理员注册码不正确");
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }
        user.setStatus(1);
        user.setRole(finalRole);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insertSelective(user);
        // 注册成功后自动登录，返回token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole() == null ? 0 : user.getRole());
        data.put("status", user.getStatus() == null ? 1 : user.getStatus());
        return Result.success(data);
    }

    // 更新头像
    public boolean updateAvatar(Long userId, String avatarUrl) {
        int rows = userMapper.updateAvatarById(userId, avatarUrl);
        return rows > 0;
    }

    // 保存头像
    private String saveAvatar(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + suffix;

        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/avatars/";
        File dest = new File(uploadDir + newFileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);

        return "/avatars/" + newFileName;
    }

    // 新增：根据 ID 查询用户
    public User getUserById(Long userId) {
        return userMapper.selectByPrimaryKey(userId);
    }

    // 新增：根据用户名查询用户
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    // ========== 修改密码 ==========
    public Result<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.fail(400, "旧密码不正确");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail(400, "新密码至少6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateByPrimaryKeySelective(user);
        return Result.success(null);
    }

    // ========== 更新个人资料 ==========
    public Result<Void> updateProfile(User user) {
        User existing = getUserById(user.getId());
        if (existing == null) {
            return Result.fail(404, "用户不存在");
        }
        if (user.getUsername() != null)
            existing.setUsername(user.getUsername());
        if (user.getEmail() != null)
            existing.setEmail(user.getEmail());
        if (user.getPhone() != null)
            existing.setPhone(user.getPhone());
        if (user.getAvatar() != null)
            existing.setAvatar(user.getAvatar());
        userMapper.updateByPrimaryKeySelective(existing);
        return Result.success(null);
    }

    // ========== 提交反馈 ==========
    public Result<Void> submitFeedback(Long userId, String type, String content, String contact) {
        // 如果有 Feedback 表，存入数据库
        // 没有的话先打印日志，返回成功
        System.out.println("=== 收到用户反馈 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("类型: " + type);
        System.out.println("内容: " + content);
        System.out.println("联系方式: " + contact);
        return Result.success(null);
    }
}
