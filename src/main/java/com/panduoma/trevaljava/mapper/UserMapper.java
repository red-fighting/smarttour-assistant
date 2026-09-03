package com.panduoma.trevaljava.mapper;

import com.panduoma.trevaljava.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
/**
* @author 123
* @description 针对表【user(用户信息表)】的数据库操作Mapper
* @createDate 2026-06-27 13:29:23
* @Entity com.panduoma.trevaljava.entity.User
*/
@Mapper     //让MyBatis识别Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Long id);
    int insert(User record);
    int updateByPrimaryKeySelective(User record);
    int updateByPrimaryKey(User record);
    // 新增：查询所有有效用户
    List<User> selectAllValidUser();
    // 根据 id 查询用户（统一使用 Long 类型，避免 int/Long 方法冲突导致 MyBatis Invalid bound statement）
    User selectByPrimaryKey(Long id);
    // 根据用户名查询是否存在（XML 中已定义）
    User selectByUsername(String username);
    // 根据邮箱查询用户（XML 中已定义）
    User selectByEmail(String email);
    // 根据手机号查询用户（XML 中已定义）
    User selectByPhone(String phone);

    @Update("UPDATE user SET avatar = #{avatar} WHERE id = #{id}")
    int updateAvatarById(@Param("id") Long id, @Param("avatar") String avatar);

    // insertSelective 已在 UserMapper.xml 中定义，不再使用注解，避免重复注册导致整个 Mapper 加载失败
    int insertSelective(User user);

    // 查询所有用户（管理员用）
    @Select("SELECT * FROM user ORDER BY id DESC")
    List<User> selectAllUsers();

    // 更新用户状态（禁用/启用）
    @Update("UPDATE user SET status = #{status} WHERE id = #{id}")
    int updateUserStatus(@Param("id") Long id, @Param("status") int status);

}
