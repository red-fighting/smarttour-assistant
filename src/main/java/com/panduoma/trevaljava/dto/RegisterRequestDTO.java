package com.panduoma.trevaljava.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "姓名不为空")
    private String username;
    @NotBlank(message = "密码不为空")
    private String password;
    @NotBlank(message = "确认密码不为空")
    private String confirmPassword;
    @NotBlank(message = "电话号码不为空")
    private String phone;
    @NotBlank(message = "邮箱号不为空")
    private String email;
}
