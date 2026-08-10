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
    User selectByPrimaryKey(int id);
    // 根据用户名查询是否存在
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    @Update("UPDATE user SET avatar = #{avatar}, update_time = NOW() WHERE id = #{id}")
    int updateAvatarById(@Param("id") Long id, @Param("avatar") String avatar);

    @Insert("INSERT INTO user (username, password, phone, email, create_time) " +
            "VALUES (#{username}, #{password}, #{phone}, #{email}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertSelective(User user);
    User selectByPrimaryKey(Long id);

}
