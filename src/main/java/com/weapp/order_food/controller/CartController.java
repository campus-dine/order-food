package com.weapp.order_food.controller;


import com.weapp.order_food.model.dto.CartOperationDTO;
import com.weapp.order_food.model.dto.CartQueryDTO;
import com.weapp.order_food.model.vo.CartVO;
import com.weapp.order_food.service.CartService;
import com.weapp.order_food.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "客户购物车模块")
@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @ApiOperation("获取当前用户购物车内所有物品")
    @GetMapping("/list")
    public Result<List<CartVO>> getCartList(CartQueryDTO dto) {
        if (dto.getCustomerId() == null) {
            return Result.error("客户ID不能为空");
        }
        List<CartVO> list = cartService.getCartListByCustomerId(dto);
        return Result.success(list);
    }

    @ApiOperation("点击加入购物车(数量+1)")
    @PostMapping("/add")
    public Result<List<CartVO>> addIntoCart(@RequestBody CartOperationDTO dto) {
        if (dto.getCustomerId() == null || dto.getDishId() == null) {
            return Result.error("参数不完整");
        }
        // 执行完加车动作后，按照你的要求：把更新后的全部购物车信息实时打包传给前端
        List<CartVO> updatedList = cartService.addQuantity(dto);
        return Result.success(updatedList);
    }

    @ApiOperation("点击减少/删除购物车(数量-1)")
    @PostMapping("/sub")
    public Result<List<CartVO>> subFromCart(@RequestBody CartOperationDTO dto) {
        if (dto.getCustomerId() == null || dto.getDishId() == null) {
            return Result.error("参数不完整");
        }
        List<CartVO> updatedList = cartService.subQuantity(dto);
        return Result.success(updatedList);
    }
}