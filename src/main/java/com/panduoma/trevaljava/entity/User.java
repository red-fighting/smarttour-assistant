package com.panduoma.trevaljava.entity;

import java.util.Date;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * 用户信息表
 * @TableName user
 */
@Data
public class User {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户姓名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 角色：0=普通用户，1=管理员
     */
    private Integer role;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}