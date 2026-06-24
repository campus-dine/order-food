package com.weapp.order_food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weapp.order_food.entity.Cart;
import com.weapp.order_food.model.vo.CartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    @Select("SELECT c.id, c.dish_id AS dishId, d.dish_name AS dishName, d.image_url AS imageUrl, " +
            "c.added_price AS addedPrice, c.quantity " +
            "FROM carts c " +
            "LEFT JOIN dishes d ON c.dish_id = d.id " +
            "WHERE c.customer_id = #{customerId}")
    List<CartVO> getCartDetailsByCustomerId(@Param("customerId") Long customerId);
}