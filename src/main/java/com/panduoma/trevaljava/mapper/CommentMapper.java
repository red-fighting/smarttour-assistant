package com.panduoma.trevaljava.mapper;

import com.panduoma.trevaljava.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO share_comment (share_id, user_id, username, avatar, text) " +
            "VALUES (#{shareId}, #{userId}, #{username}, #{avatar}, #{text})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Comment comment);

    @Select("SELECT * FROM share_comment WHERE share_id = #{shareId} ORDER BY create_time ASC")
    List<Comment> selectByShareId(@Param("shareId") Long shareId);

    @Delete("DELETE FROM share_comment WHERE share_id = #{shareId}")
    int deleteByShareId(@Param("shareId") Long shareId);
}
