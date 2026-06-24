package com.weapp.order_food.service;

import com.weapp.order_food.model.dto.CartOperationDTO;
import com.weapp.order_food.model.dto.CartQueryDTO;
import com.weapp.order_food.model.vo.CartVO;

import java.util.List;

public interface CartService {
    List<CartVO> getCartListByCustomerId(CartQueryDTO dto);
    List<CartVO> addQuantity(CartOperationDTO dto);
    List<CartVO> subQuantity(CartOperationDTO dto);
}
