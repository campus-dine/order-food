package com.weapp.order_food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weapp.order_food.entity.*;
import com.weapp.order_food.mapper.*;
import com.weapp.order_food.model.dto.*;
import com.weapp.order_food.model.vo.OrderDetailVO;
import com.weapp.order_food.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final OrderDeliveryMapper orderDeliveryMapper;
    private final CartMapper cartMapper;
    private final DishMapper dishMapper;
    private static final AtomicInteger atomicCode = new AtomicInteger((int)(Math.random() * 100000));

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitOrder(OrderCreateDTO dto) {
        log.info("订单模块：执行高安全级校验并准备创建订单...");

        // 1. 批量查询该用户当前购物车里的所有真实数据
        List<Cart> dbCarts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getCustomerId, dto.getCustomerId()));

        java.util.Map<Long, Integer> dbCartMap = dbCarts.stream()
                .collect(java.util.stream.Collectors.toMap(Cart::getDishId, Cart::getQuantity));

        // 收集前端传过来想要结算的所有菜品ID
        List<Long> dishIdsToClear = new ArrayList<>();

        // 2. 强校验：比对数量与库存
        for (OrderDishDTO dishDto : dto.getDishes()) {
            Long dishId = dishDto.getDishId();
            Integer frontQty = dishDto.getQuantity();

            // 校验 A：购物车数据对齐
            if (!dbCartMap.containsKey(dishId) || !dbCartMap.get(dishId).equals(frontQty)) {
                throw new RuntimeException("操作失败：购物车数据不一致，请刷新后重试");
            }

            // 校验 B：库存校验
            Dish dish = dishMapper.selectById(dishId);
            if (dish == null || dish.getStatus() == 0) {
                throw new RuntimeException("操作失败：菜品已下架");
            }
            if (dish.getStock() < frontQty) {
                throw new RuntimeException("库存不足：菜品 [" + dish.getDishName() + "] 仅剩 " + dish.getStock() + " 份");
            }

            // 3. 同步扣减商品库存
            dish.setStock(dish.getStock() - frontQty);
            dishMapper.updateById(dish);

            dishIdsToClear.add(dishId);
        }

        // 4. 🚨 落实批量删除：直接调用一条 SQL 抹除用户选中的这些商品，拒绝任何循环删除！
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getCustomerId, dto.getCustomerId())
                .in(Cart::getDishId, dishIdsToClear)
        );
        log.info("订单模块：购物车商品已成功执行批量单条SQL删除！");

        // 5. 生成 6 位不冲突取餐码
        int nextId = atomicCode.incrementAndGet();
        String pickupCode = String.format("%06d", Math.abs(nextId % 1000000));

        // 6. 创建订单主表
        String orderNo = "ORD_" + System.currentTimeMillis();
        Order order = Order.builder()
                .customerId(dto.getCustomerId())
                .merchantId(dto.getMerchantId())
                .orderNo(orderNo)
                .pickupCodeId(pickupCode)
                .dineType(dto.getDineType())
                .bookTime(dto.getBookTime())
                .totalAmount(dto.getTotalAmount())
                .actualAmount(dto.getActualAmount())
                .orderStatus(1) // 1-待制作/接单
                .build();
        this.save(order);

        // 7. 写入订单子表明细
        for (OrderDishDTO dishDto : dto.getDishes()) {
            Dish dish = dishMapper.selectById(dishDto.getDishId());
            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .dishId(dishDto.getDishId())
                    .quantity(dishDto.getQuantity())
                    .dishName(dish.getDishName())
                    .payPrice(dish.getPrice())
                    .remark(dishDto.getRemark())
                    .build();
            orderItemMapper.insert(item);
        }

        // 8. 联动外送表
        if (Boolean.TRUE.equals(dto.getIsDelivery())) {
            OrderDelivery delivery = OrderDelivery.builder()
                    .orderId(order.getId())
                    .addressId(dto.getAddressId())
                    .orderNo(orderNo)
                    .deliveryFee(dto.getDeliveryFee())
                    .status(0) // 0-待接单
                    .build();
            orderDeliveryMapper.insert(delivery);
        }

        return orderNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeOrderStatus(OrderStatusUpdateDTO dto) {
        log.info("订单状态流转：订单ID {}，目标状态 {}", dto.getOrderId(), dto.getTargetStatus());

        if (dto.getTargetStatus() == 4) {
            OrderDelivery delivery = orderDeliveryMapper.selectOne(new LambdaQueryWrapper<OrderDelivery>()
                    .eq(OrderDelivery::getOrderId, dto.getOrderId()));

            if (delivery != null && delivery.getStatus() == 0) {
                throw new RuntimeException("外卖尚未开始配送，无法提前确认完成订单！");
            }
        }

        this.update(new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Order>()
                .eq("id", dto.getOrderId())
                .set("order_status", dto.getTargetStatus())
        );

        if (dto.getDeliveryStatus() != null) {
            orderDeliveryMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<OrderDelivery>()
                    .eq("order_id", dto.getOrderId())
                    .set("status", dto.getDeliveryStatus())
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String verifyAndCompleteOrder(MerchantVerifyCodeDTO dto) {
        log.info("商家核销：商家 {} 正在验证取餐码 {}", dto.getMerchantId(), dto.getPickupCode());

        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getPickupCodeId, dto.getPickupCode())
                .eq(Order::getMerchantId, dto.getMerchantId())
                .notIn(Order::getOrderStatus, 4, 5)
                .last("LIMIT 1"));

        if (order == null) {
            throw new RuntimeException("核销失败：取餐码无效或订单已被处理");
        }

        OrderDelivery delivery = orderDeliveryMapper.selectOne(new LambdaQueryWrapper<OrderDelivery>()
                .eq(OrderDelivery::getOrderId, order.getId()));

        if (delivery == null) {
            order.setOrderStatus(4);
            this.updateById(order);
            return "核销成功：该订单为自取/堂食，已直接完成订单！";
        } else {
            order.setOrderStatus(3);
            this.updateById(order);

            orderDeliveryMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<OrderDelivery>()
                    .eq("order_id", order.getId())
                    .set("status", 1)
            );
            return "验证成功：该订单为外送订单，已成功转为【配送中】状态，请安排配送！";
        }
    }


    @Override
    public List<Order> getHistoryOrderList(OrderHistoryQueryDTO dto) {
        log.info("订单模块：正在获取用户 {} 的历史订单...", dto.getCustomerId());
        // 按照创建时间倒序排列，新订单在最上面
        return this.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getCustomerId, dto.getCustomerId())
                .orderByDesc(Order::getCreateTime));
    }

    @Override
    public OrderDetailVO getOrderDetailWithItems(OrderDetailQueryDTO dto) {
        log.info("订单模块：获取详情，订单ID: {}, 要求状态: {}", dto.getOrderId(), dto.getStatus());

        // 1. 查找主表，并且严格限定前端传过来的状态要求
        Order order = this.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, dto.getOrderId())
                .eq(Order::getOrderStatus, dto.getStatus()));

        if (order == null) {
            return null; // 状态不符或订单不存在，直接拦截
        }

        // 2. 查出该订单子表关联的所有菜品明细
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));

        // 3. 检查有没有外送附加表信息
        OrderDelivery delivery = orderDeliveryMapper.selectOne(new LambdaQueryWrapper<OrderDelivery>()
                .eq(OrderDelivery::getOrderId, order.getId()));

        // 4. 完美打包组装 VO
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPickupCodeId(order.getPickupCodeId());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setDineType(order.getDineType());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setActualAmount(order.getActualAmount());
        vo.setBookTime(order.getBookTime());
        vo.setCreateTime(order.getCreateTime());

        // 🚨 装填子表数据
        vo.setOrderItems(items);
        // 如果有外送数据，顺便把外送状态捎带上
        vo.setDeliveryStatus(delivery != null ? delivery.getStatus() : null);

        return vo;
    }
}