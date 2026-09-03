package com.panduoma.trevaljava.mapper;

import com.panduoma.trevaljava.entity.Banner;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BannerMapper {

    /** 首页轮播：只取启用状态，按 sort_order 升序 */
    @Select("SELECT * FROM banner WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<Banner> selectEnabled();

    /** 管理后台：全部轮播图（含禁用） */
    @Select("SELECT * FROM banner ORDER BY sort_order ASC, id ASC")
    List<Banner> selectAll();

    @Select("SELECT * FROM banner WHERE id = #{id}")
    Banner selectById(@Param("id") Long id);

    @Insert("INSERT INTO banner (title, subtitle, image_url, sort_order, status, create_time) " +
            "VALUES (#{title}, #{subtitle}, #{imageUrl}, #{sortOrder}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Banner banner);

    @Update("UPDATE banner SET title = #{title}, subtitle = #{subtitle}, image_url = #{imageUrl}, " +
            "sort_order = #{sortOrder}, status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(Banner banner);

    @Delete("DELETE FROM banner WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
