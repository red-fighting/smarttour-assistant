package com.panduoma.trevaljava.mapper;

import com.panduoma.trevaljava.entity.Share;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ShareMapper {

    @Insert("INSERT INTO share (user_id, title, content, images, tag, likes, comments, create_time) " +
            "VALUES (#{userId}, #{title}, #{content}, #{images}, #{tag}, #{likes}, #{comments}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Share share);

    @Update("UPDATE share SET likes = #{likes}, comments = #{comments} WHERE id = #{id}")
    int updateInteractions(@Param("id") Long id, @Param("likes") int likes, @Param("comments") int comments);

    @Delete("DELETE FROM share WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM share WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Share> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM share ORDER BY create_time DESC")
    List<Share> selectAll();

    @Select("SELECT COUNT(*) FROM share WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /** 查所有分享 + JOIN user 表拿发帖人 username / avatar */
    @Select("SELECT s.*, " +
            "IFNULL(u.username, '') AS post_username, " +
            "IFNULL(u.avatar, '') AS post_avatar " +
            "FROM share s LEFT JOIN user u ON s.user_id = u.id " +
            "ORDER BY s.create_time DESC")
    List<Map<String, Object>> selectAllWithUser();
}
