package com.panduoma.trevaljava.service;


import com.panduoma.trevaljava.dto.LoginRequestDTO;
import com.panduoma.trevaljava.entity.User;
import com.panduoma.trevaljava.mapper.UserMapper;
import com.panduoma.trevaljava.utils.JwtUtils;
import com.panduoma.trevaljava.utils.PasswordEncoder;
import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired      // 自动从容器拿 UserMapper 对象赋值给这个变量
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;

    //登录d
    public Result<Map<String, Object>> login(LoginRequestDTO dto){
        User user=userMapper.selectByUsername(dto.getUsername());
        // 判断用户名是否存在
        if(user==null){
            System.out.println("❌ 用户不存在");
            return Result.fail(400,"该用户不存在,请先注册");
        }
        if(user.getStatus()==0){
            System.out.println("账户已被封禁");
            return Result.fail(400,"账户已被封禁");
        }
        if(!passwordEncoder.matches(dto.getPassword(),user.getPassword())){
            return Result.fail(400,"用户名或密码错误");
        }
        //生成Token
        String token= jwtUtils.generateToken(user.getId(),user.getUsername());
        Map<String,Object> data=new HashMap<>();
        data.put("token",token);
        data.put("userId",user.getId());
        data.put("username",user.getUsername());
        return Result.success(data);
    }

    //注册
    public Result<Map<String, Object>> register(String username,
                                                String password,
                                                String confirmPassword,
                                                String phone,
                                                String email)throws IOException{
        //检查密码和确认密码
        if (!password.equals(confirmPassword)) {
            return Result.fail(400, "两次输入密码不一致");
        }


        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insertSelective(user);
        //注册成功后自动登录，返回token
        String token=jwtUtils.generateToken(user.getId(),user.getUsername());
        Map<String,Object> data=new HashMap<>();
        data.put("token",token);
        data.put("userId",user.getId());
        data.put("username",user.getUsername());
        return Result.success(data);
    }
    //更新头像
    public boolean updateAvatar(Long userId, String avatarUrl) {
        int rows = userMapper.updateAvatarById(userId, avatarUrl);
        return rows > 0;
    }
    //保存头像
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


}
