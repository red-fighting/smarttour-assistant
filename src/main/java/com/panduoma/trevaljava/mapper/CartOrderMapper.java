package com.panduoma.trevaljava.mapper;

import com.panduoma.trevaljava.entity.CartOrder;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CartOrderMapper {

    /** 插入购物车记录（含出行日期） */
    @Insert("INSERT INTO cart_order (user_id, product_id, quantity, price, title_snapshot, image_snapshot, status, start_date, end_date, create_time) " +
            "VALUES (#{userId}, #{productId}, #{quantity}, #{price}, #{titleSnapshot}, #{imageSnapshot}, #{status}, #{startDate}, #{endDate}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(CartOrder cartOrder);

    /** 查询用户所有记录（按 status 过滤）：0=购物车 1=已支付，-1=全部 */
    @Select("<script>" +
            "SELECT * FROM cart_order WHERE user_id = #{userId} " +
            "<if test='status != null and status != -1'> AND status = #{status} </if>" +
            "ORDER BY status ASC, create_time DESC" +
            "</script>")
    List<CartOrder> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /** 查询该用户是否已有未支付的该商品（同商品 + 同出发日期 才合并数量，不同日期视为不同购物车项） */
    @Select("SELECT * FROM cart_order WHERE user_id = #{userId} AND product_id = #{productId} AND status = 0 " +
            "AND start_date = #{startDate} AND end_date = #{endDate} LIMIT 1")
    CartOrder selectCartExisting(@Param("userId") Long userId, @Param("productId") Long productId,
                                 @Param("startDate") java.time.LocalDate startDate,
                                 @Param("endDate") java.time.LocalDate endDate);

    /** 更新数量（重复加入时） */
    @Update("UPDATE cart_order SET quantity = quantity + #{addQuantity}, update_time = NOW() WHERE id = #{id}")
    int incQuantity(@Param("id") Long id, @Param("addQuantity") int addQuantity);

    /** 删除 */
    @Delete("DELETE FROM cart_order WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /** 支付：status 0→1 + 写 paid_time */
    @Update("UPDATE cart_order SET status = 1, paid_time = NOW(), update_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND status = 0")
    int payById(@Param("id") Long id, @Param("userId") Long userId);

    /** 批量支付用户所有购物车 */
    @Update("UPDATE cart_order SET status = 1, paid_time = NOW(), update_time = NOW() WHERE user_id = #{userId} AND status = 0")
    int payAllByUserId(@Param("userId") Long userId);

    /** 查记录数 */
    @Select("SELECT COUNT(*) FROM cart_order WHERE user_id = #{userId} AND status = #{status}")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") int status);

    /* ============ 管理员用：全表查询 ============ */

    /** 查询全部订单（可选按 status 过滤：-1 表示全部） */
    @Select("<script>" +
            "SELECT co.*, u.username AS user_name FROM cart_order co " +
            "LEFT JOIN user u ON co.user_id = u.id " +
            "<where>" +
            "  <if test='status != null and status != -1'> AND co.status = #{status} </if>" +
            "</where>" +
            "ORDER BY co.create_time DESC" +
            "</script>")
    List<java.util.Map<String, Object>> selectAllOrders(@Param("status") Integer status);

    /** 按状态统计订单数（用于仪表盘） */
    @Select("SELECT status, COUNT(*) AS cnt, COALESCE(SUM(price * quantity),0) AS amount FROM cart_order GROUP BY status")
    List<java.util.Map<String, Object>> selectOrderStats();

    /** 管理员更新订单状态（强制 0/1） */
    @Update("UPDATE cart_order SET status = #{status}, update_time = NOW(), " +
            "paid_time = CASE WHEN #{status} = 1 THEN COALESCE(paid_time, NOW()) ELSE NULL END " +
            "WHERE id = #{id}")
    int updateOrderStatus(@Param("id") Long id, @Param("status") int status);

    /** 管理员删除订单 */
    @Delete("DELETE FROM cart_order WHERE id = #{id}")
    int deleteOrderById(@Param("id") Long id);
}
