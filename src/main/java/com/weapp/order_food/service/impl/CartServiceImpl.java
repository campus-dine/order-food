package com.weapp.order_food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weapp.order_food.entity.Cart;
import com.weapp.order_food.entity.Dish;
import com.weapp.order_food.mapper.CartMapper;
import com.weapp.order_food.mapper.DishMapper;
import com.weapp.order_food.model.dto.CartOperationDTO;
import com.weapp.order_food.model.dto.CartQueryDTO;
import com.weapp.order_food.model.vo.CartVO;
import com.weapp.order_food.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 购物车业务实现类
 * 写接口时：返回购物车列表时，必须同时查出 carts.added_price 和 dishes.current_price，计算 降价差额 = dishes.current_price - carts.added_price 并返回给前端用于展示“降价标签”。
 *
 * 写结算时：绝对不要用 carts.added_price 累加，重新查一次 dishes 表拿最新价格来算总价。
 */


@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final DishMapper dishMapper; // 需要查询菜品实时售价

    @Override
    public List<CartVO> getCartListByCustomerId(CartQueryDTO dto) {
        return this.baseMapper.getCartDetailsByCustomerId(dto.getCustomerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CartVO> addQuantity(CartOperationDTO dto) {
        log.info("购物车：用户 {} 对菜品 {} 执行加车", dto.getCustomerId(), dto.getDishId());

        // 1. 获取菜品的实时信息（主要是拿最新单价，同时确认菜品合法）
        Dish dish = dishMapper.selectById(dto.getDishId());
        if (dish == null || dish.getStatus() == 0) {
            throw new RuntimeException("该菜品不存在或已下架");
        }

        // 2. 查表看该用户购物车中是否已经有了这款菜
        Cart cartItem = this.getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getCustomerId, dto.getCustomerId())
                .eq(Cart::getDishId, dto.getDishId()));

        if (cartItem == null) {
            // 🚨 逻辑：表中没有对应数据 -> 新增一条记录，初始数量为 1
            Cart newCart = Cart.builder()
                    .customerId(dto.getCustomerId())
                    .dishId(dto.getDishId())
                    .addedPrice(dish.getPrice()) // 锁定当前加入时售价
                    .quantity(1)
                    .build();
            this.save(newCart);
        } else {
            // 🚨 逻辑：表中有数据 -> 增加菜品数量，并更新这条信息的菜品价格
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItem.setAddedPrice(dish.getPrice()); // 同步更新价格为最新价
            this.updateById(cartItem);
        }

        // 3. 将更新后的全部购物车信息打包成 VO 清单返还
        return this.baseMapper.getCartDetailsByCustomerId(dto.getCustomerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CartVO> subQuantity(CartOperationDTO dto) {
        log.info("购物车：用户 {} 对菜品 {} 执行减车", dto.getCustomerId(), dto.getDishId());

        // 1. 查表发现对应的购物车记录
        Cart cartItem = this.getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getCustomerId, dto.getCustomerId())
                .eq(Cart::getDishId, dto.getDishId()));

        // 🚨 逻辑：如果没有对应的数据，直接抛出异常（返回错误）
        if (cartItem == null) {
            throw new RuntimeException("购物车内无该菜品，无法减少数量");
        }

        // 2. 如果有数据，减少一次菜品数量
        int remainingQuantity = cartItem.getQuantity() - 1;
        if (remainingQuantity <= 0) {
            // 数量减到0了，直接从购物车中移除这条商品
            this.removeById(cartItem.getId());
        } else {
            // 数量还大于0，更新数量。同时按照你的逻辑，顺便同步一次菜品最新的市场价格
            Dish dish = dishMapper.selectById(dto.getDishId());
            if (dish != null) {
                cartItem.setAddedPrice(dish.getPrice());
            }
            cartItem.setQuantity(remainingQuantity);
            this.updateById(cartItem);
        }

        // 3. 打包最新列表返还前端
        return this.baseMapper.getCartDetailsByCustomerId(dto.getCustomerId());
    }
}