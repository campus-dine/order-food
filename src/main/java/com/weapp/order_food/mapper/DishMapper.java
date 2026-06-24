package com.weapp.order_food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weapp.order_food.entity.Dish;
import com.weapp.order_food.model.vo.RecipeDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    /**
     * 🚨 一行 SQL 搞定多表联查：根据菜品ID，查出配方表以及对应的配料名称
     */
    @Select("SELECT r.ingredient_id AS ingredientId, i.name AS ingredientName, " +
            "r.usage_amount AS usageAmount, r.usage_unit AS usageUnit, r.cost AS cost " +
            "FROM dish_recipes r " +
            "LEFT JOIN ingredients i ON r.ingredient_id = i.id " +
            "WHERE r.dish_id = #{dishId}")
    List<RecipeDetailVO> getRecipesByDishId(@Param("dishId") Long dishId);
}
