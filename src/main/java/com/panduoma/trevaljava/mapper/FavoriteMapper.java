package com.panduoma.trevaljava.mapper;

import com.panduoma.trevaljava.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT INTO favorite (user_id, post_id, post_title, post_content, post_images, post_tag, post_username, post_avatar, post_location, create_time) " +
            "VALUES (#{userId}, #{postId}, #{postTitle}, #{postContent}, #{postImages}, #{postTag}, #{postUsername}, #{postAvatar}, #{postLocation}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Favorite favorite);

    @Delete("DELETE FROM favorite WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM favorite WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Favorite> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND post_id = #{postId} LIMIT 1")
    Favorite selectByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
