package com.weapp.order_food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weapp.order_food.entity.Dish;
import com.weapp.order_food.mapper.DishMapper;
import com.weapp.order_food.model.dto.DishDetailQueryDTO;
import com.weapp.order_food.model.dto.DishPageQueryDTO;
import com.weapp.order_food.model.vo.DishDetailVO;
import com.weapp.order_food.model.vo.DishScrollVO;
import com.weapp.order_food.model.vo.RecipeDetailVO;
import com.weapp.order_food.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Override
    public DishScrollVO getDishScrollListByCategory(DishPageQueryDTO dto) {
        Long categoryId = dto.getCategoryId();
        Long lastId = dto.getLastId();
        Integer lastSales = dto.getLastSales();

        log.info("菜品展示：收到前端 DTO 查询，分类: {}, 锚点ID: {}, 锚点销量: {}", categoryId, lastId, lastSales);

        // 1. 查总数
        LambdaQueryWrapper<Dish> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Dish::getCategoryId, categoryId).gt(Dish::getStock, 0).eq(Dish::getStatus, 1);
        long totalCount = this.count(countWrapper);

        // 2. 分页器限制 10 条
        Page<Dish> limitPage = new Page<>(1, 10);

        // 3. 构建条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, categoryId).gt(Dish::getStock, 0).eq(Dish::getStatus, 1);

        if (lastId == -1) {
            // 第一次查询：直接按销量和ID倒序排序
            queryWrapper.orderByDesc(Dish::getSales).orderByDesc(Dish::getId);
        } else {
            // 流式滚动：边界切片过滤
            queryWrapper.and(wrapper -> wrapper
                    .lt(Dish::getSales, lastSales)
                    .or(w -> w.eq(Dish::getSales, lastSales).lt(Dish::getId, lastId))
            );
            queryWrapper.orderByDesc(Dish::getSales).orderByDesc(Dish::getId);
        }

        // 4. 查库
        List<Dish> dishList = this.baseMapper.selectPage(limitPage, queryWrapper).getRecords();

        // 5. 组装 VO 返回
        DishScrollVO vo = new DishScrollVO();
        vo.setList(dishList);
        vo.setTotal(totalCount);

        if (dishList != null && !dishList.isEmpty()) {
            Dish lastDish = dishList.get(dishList.size() - 1);
            vo.setLastId(lastDish.getId());
            vo.setLastSales(lastDish.getSales());
        } else {
            vo.setLastId(lastId);
            vo.setLastSales(lastSales);
        }

        return vo;
    }

    @Override
    public DishDetailVO getDishDetailWithRecipes(DishDetailQueryDTO dto) {
        Long dishId = dto.getDishId();
        log.info("菜品详情：正在查询菜品 {} 及其完整的配方配料清单", dishId);

        // 1. 根据菜品id，让 mapper 查找菜品基础信息
        Dish dish = this.baseMapper.selectById(dishId);
        if (dish == null || dish.getStatus() == 0) {
            return null; // 菜品不存在或已被商家下架
        }

        // 2. 调用刚刚手写的连表 Mapper 方法，一网打尽配方和配料名称
        List<RecipeDetailVO> recipes = this.baseMapper.getRecipesByDishId(dishId);

        // 3. 完美组装成最终的 VO
        DishDetailVO vo = new DishDetailVO();
        vo.setId(dish.getId());
        vo.setDishName(dish.getDishName());
        vo.setImageUrl(dish.getImageUrl());
        vo.setCategoryId(dish.getCategoryId());
        vo.setRating(dish.getRating());
        vo.setStock(dish.getStock());
        vo.setPrice(dish.getPrice());

        // 🚨 塞入组装好的配方列表
        vo.setRecipes(recipes);

        return vo;
    }
}