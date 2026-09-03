package com.panduoma.trevaljava.mapper;

import com.panduoma.trevaljava.entity.Product;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT * FROM product ORDER BY sold_count DESC, id DESC")
    List<Product> selectAll();

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product selectById(@Param("id") Long id);

    @Update("UPDATE product SET sold_count = sold_count + #{inc} WHERE id = #{id}")
    int incSoldCount(@Param("id") Long id, @Param("inc") int inc);

    @Insert("INSERT INTO product (title, origin, destination, duration, price, image, images, route_desc, food_feature, guide_name, guide_phone, sold_count, create_time) " +
            "VALUES (#{title}, #{origin}, #{destination}, #{duration}, #{price}, #{image}, #{images}, #{routeDesc}, #{foodFeature}, #{guideName}, #{guidePhone}, #{soldCount}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Product product);

    @Update("UPDATE product SET title = #{title}, origin = #{origin}, destination = #{destination}, duration = #{duration}, " +
            "price = #{price}, image = #{image}, images = #{images}, route_desc = #{routeDesc}, food_feature = #{foodFeature}, " +
            "guide_name = #{guideName}, guide_phone = #{guidePhone}, update_time = NOW() WHERE id = #{id}")
    int update(Product product);

    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /** 动态条件查询：起始地/目的地/预算/天数，全部可选 */
    @Select("<script>" +
            "SELECT * FROM product " +
            "<where>" +
            "  <if test='origin != null and origin != \"\"'>AND origin LIKE CONCAT('%', #{origin}, '%')</if>" +
            "  <if test='destination != null and destination != \"\"'>AND destination LIKE CONCAT('%', #{destination}, '%')</if>" +
            "  <if test='budget != null'>AND price &lt;= #{budget}</if>" +
            "  <if test='duration != null'>AND duration = #{duration}</if>" +
            "</where>" +
            "ORDER BY sold_count DESC, id DESC" +
            "</script>")
    List<Product> searchProducts(@Param("origin") String origin,
                                 @Param("destination") String destination,
                                 @Param("budget") Double budget,
                                 @Param("duration") Integer duration);
}
