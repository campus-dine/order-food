package com.weapp.order_food.service;

import com.weapp.order_food.entity.Order;
import com.weapp.order_food.model.dto.*;
import com.weapp.order_food.model.vo.OrderDetailVO;

import java.util.List;

public interface OrderService {
    String submitOrder(OrderCreateDTO dto);
    void changeOrderStatus(OrderStatusUpdateDTO dto);
    String verifyAndCompleteOrder(MerchantVerifyCodeDTO dto);
    List<Order> getHistoryOrderList(OrderHistoryQueryDTO dto);
    OrderDetailVO getOrderDetailWithItems(OrderDetailQueryDTO dto);
}
