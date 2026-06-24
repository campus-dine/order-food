package com.weapp.order_food.service;

import com.weapp.order_food.model.dto.DishDetailQueryDTO;
import com.weapp.order_food.model.dto.DishPageQueryDTO;
import com.weapp.order_food.model.vo.DishDetailVO;
import com.weapp.order_food.model.vo.DishScrollVO;

public interface DishService {
    DishScrollVO getDishScrollListByCategory(DishPageQueryDTO dto);
    DishDetailVO getDishDetailWithRecipes(DishDetailQueryDTO dto);
}
