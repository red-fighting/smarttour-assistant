package com.panduoma.trevaljava.controller;

import com.panduoma.trevaljava.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class UploadController {

    /** 上传目录（绝对路径，项目根目录下的 uploads/） */
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** 上传图片 → 存服务器本地 → 返回可访问 URL */
    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "请选择文件");
        }
        // 校验类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "只能上传图片");
        }
        // 校验大小（10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.fail(400, "图片不能超过 10MB");
        }

        try {
            // 确保目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 生成唯一文件名：UUID + 原始扩展名
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            File dest = new File(dir, filename);
            file.transferTo(dest);

            // 返回可通过 Spring Boot 静态资源映射访问的 URL
            String url = "/uploads/" + filename;
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", filename);
            return Result.success(data);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.fail(500, "图片保存失败: " + e.getMessage());
        }
    }
}
