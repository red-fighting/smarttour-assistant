package com.panduoma.trevaljava.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShareLikeMapper {

    @Insert("INSERT INTO share_like (share_id, user_id) VALUES (#{shareId}, #{userId})")
    int insertLike(@Param("shareId") Long shareId, @Param("userId") Long userId);

    @Delete("DELETE FROM share_like WHERE share_id = #{shareId} AND user_id = #{userId}")
    int deleteLike(@Param("shareId") Long shareId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM share_like WHERE share_id = #{shareId} AND user_id = #{userId}")
    int countByShareAndUser(@Param("shareId") Long shareId, @Param("userId") Long userId);

    @Select("SELECT share_id FROM share_like WHERE user_id = #{userId}")
    List<Long> selectShareIdsByUserId(@Param("userId") Long userId);
}
