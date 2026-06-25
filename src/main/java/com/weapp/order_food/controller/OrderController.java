package com.weapp.order_food.controller;

import com.weapp.order_food.entity.Order;
import com.weapp.order_food.model.dto.*;
import com.weapp.order_food.model.vo.OrderDetailVO;
import com.weapp.order_food.service.OrderService;
import com.weapp.order_food.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "订单处理模块")
@RestController
@RequestMapping("/api/customer/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @ApiOperation("购物车结算：创建订单并清空对应购物车商品")
    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody OrderCreateDTO dto) {
        if (dto.getCustomerId() == null || dto.getDishes() == null || dto.getDishes().isEmpty()) {
            return Result.error("订单商品不能为空");
        }
        if (Boolean.TRUE.equals(dto.getIsDelivery()) && dto.getAddressId() == null) {
            return Result.error("外送订单缺少配送地址");
        }

        // 调用 Service 一键落库
        String orderNo = orderService.submitOrder(dto);
        return Result.success("success", orderNo);
    }

    @ApiOperation("修改/改变订单状态（含外送状态联动）")
    @PutMapping("/status")
    public Result<String> updateOrderStatus(@RequestBody OrderStatusUpdateDTO dto) {
        if (dto.getOrderId() == null || dto.getTargetStatus() == null) {
            return Result.error("参数不完整");
        }
        orderService.changeOrderStatus(dto);
        return Result.success("success");
    }

    @ApiOperation("商家端：输入取餐码进行核销或开启配送")
    @PutMapping("/merchant/verify-code")
    public Result<String> verifyPickupCode(@RequestBody MerchantVerifyCodeDTO dto) {
        if (dto.getPickupCode() == null || dto.getMerchantId() == null) {
            return Result.error("取餐码或商家信息不能为空");
        }

        // 调用核销核心服务
        String tipMessage = orderService.verifyAndCompleteOrder(dto);
        return Result.success(tipMessage);
    }

    @ApiOperation("客户端：查看历史订单列表")
    @GetMapping("/history-list")
    public Result<List<Order>> getHistoryList(OrderHistoryQueryDTO dto) {
        if (dto.getCustomerId() == null) {
            return Result.error("客户ID不能为空");
        }
        // 获取历史列表（这里直接返回主表记录组成的列表即可）
        List<Order> list = orderService.getHistoryOrderList(dto);
        return Result.success(list);
    }

    @ApiOperation("客户端：获取符合要求的订单详细信息（含子表及配送明细）")
    @GetMapping("/detail")
    public Result<OrderDetailVO> getOrderDetail(OrderDetailQueryDTO dto) {
        if (dto.getOrderId() == null || dto.getStatus() == null) {
            return Result.error("请求参数不完整");
        }

        OrderDetailVO detailVO = orderService.getOrderDetailWithItems(dto);
        if (detailVO == null) {
            return Result.error("未找到符合状态要求的订单");
        }
        return Result.success(detailVO);
    }
}