package com.weapp.order_food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weapp.order_food.entity.Cart;
import com.weapp.order_food.entity.Dish;
import com.weapp.order_food.mapper.CartMapper;
import com.weapp.order_food.mapper.DishMapper;
import com.weapp.order_food.model.dto.CartOperationDTO;
import com.weapp.order_food.model.dto.CartQueryDTO;
import com.weapp.order_food.model.vo.CartVO;
import com.weapp.order_food.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("购物车服务测试 - 加减车与查询")
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @SpyBean
    private CartServiceImpl cartServiceImpl;

    @MockBean
    private DishMapper dishMapper;

    @MockBean
    private CartMapper cartMapper;

    @Test
    @DisplayName("测试1: 查询用户购物车列表成功 - 包含多个商品")
    void testGetCartListByCustomerId_Success() {
        Long customerId = 1001L;
        CartQueryDTO queryDTO = new CartQueryDTO();
        queryDTO.setCustomerId(customerId);

        CartVO cartVO1 = new CartVO();
        cartVO1.setId(1L);
        cartVO1.setDishId(101L);
        cartVO1.setDishName("宫保鸡丁");
        cartVO1.setImageUrl("http://example.com/image1.jpg");
        cartVO1.setAddedPrice(new BigDecimal("28.00"));
        cartVO1.setQuantity(2);

        CartVO cartVO2 = new CartVO();
        cartVO2.setId(2L);
        cartVO2.setDishId(102L);
        cartVO2.setDishName("麻婆豆腐");
        cartVO2.setImageUrl("http://example.com/image2.jpg");
        cartVO2.setAddedPrice(new BigDecimal("18.00"));
        cartVO2.setQuantity(1);

        List<CartVO> mockCartList = Arrays.asList(cartVO1, cartVO2);

        when(cartMapper.getCartDetailsByCustomerId(customerId)).thenReturn(mockCartList);

        List<CartVO> result = cartService.getCartListByCustomerId(queryDTO);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("宫保鸡丁", result.get(0).getDishName());
        assertEquals(2, result.get(0).getQuantity());
        assertEquals("麻婆豆腐", result.get(1).getDishName());

        verify(cartMapper, times(1)).getCartDetailsByCustomerId(customerId);
    }

    @Test
    @DisplayName("测试2: 查询用户购物车列表 - 空购物车")
    void testGetCartListByCustomerId_EmptyCart() {
        Long customerId = 9999L;
        CartQueryDTO queryDTO = new CartQueryDTO();
        queryDTO.setCustomerId(customerId);

        when(cartMapper.getCartDetailsByCustomerId(customerId)).thenReturn(Arrays.asList());

        List<CartVO> result = cartService.getCartListByCustomerId(queryDTO);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(cartMapper, times(1)).getCartDetailsByCustomerId(customerId);
    }

    @Test
    @DisplayName("测试3: 新增商品到购物车 - 首次加车")
    void testAddQuantity_NewItem_FirstTimeAdd() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(101L);

        Dish dish = Dish.builder()
                .id(101L)
                .dishName("鱼香肉丝")
                .price(new BigDecimal("25.00"))
                .status(1)
                .build();

        when(dishMapper.selectById(101L)).thenReturn(dish);
        doReturn(null).when(cartServiceImpl).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(cartServiceImpl).save(any(Cart.class));
        when(cartMapper.getCartDetailsByCustomerId(1001L)).thenReturn(Arrays.asList());

        List<CartVO> result = cartService.addQuantity(operationDTO);

        assertNotNull(result);
        verify(dishMapper, times(1)).selectById(101L);
        verify(cartServiceImpl, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(cartServiceImpl, times(1)).save(argThat(cart -> 
            cart != null &&
            cart.getCustomerId().equals(1001L) &&
            cart.getDishId().equals(101L) &&
            cart.getAddedPrice().compareTo(new BigDecimal("25.00")) == 0 &&
            cart.getQuantity() == 1
        ));
    }

    @Test
    @DisplayName("测试4: 增加已有商品数量 - 非首次加车")
    void testAddQuantity_ExistingItem_IncreaseQuantity() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(102L);

        Dish dish = Dish.builder()
                .id(102L)
                .dishName("红烧肉")
                .price(new BigDecimal("35.00"))
                .status(1)
                .build();

        Cart existingCart = Cart.builder()
                .id(5L)
                .customerId(1001L)
                .dishId(102L)
                .addedPrice(new BigDecimal("33.00"))
                .quantity(2)
                .build();

        when(dishMapper.selectById(102L)).thenReturn(dish);
        doReturn(existingCart).when(cartServiceImpl).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(cartServiceImpl).updateById(any(Cart.class));
        when(cartMapper.getCartDetailsByCustomerId(1001L)).thenReturn(Arrays.asList());

        List<CartVO> result = cartService.addQuantity(operationDTO);

        assertNotNull(result);
        verify(cartServiceImpl, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(cartServiceImpl, times(1)).updateById(argThat(cart -> 
            cart != null &&
            cart.getId().equals(5L) &&
            cart.getQuantity() == 3 &&
            cart.getAddedPrice().compareTo(new BigDecimal("35.00")) == 0
        ));
        verify(cartServiceImpl, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("测试5: 增加购物车数量 - 菜品不存在抛出异常")
    void testAddQuantity_DishNotFound_ThrowsException() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(9999L);

        when(dishMapper.selectById(9999L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addQuantity(operationDTO);
        });

        assertEquals("该菜品不存在或已下架", exception.getMessage());
        verify(dishMapper, times(1)).selectById(9999L);
        verify(cartServiceImpl, never()).getOne(any(LambdaQueryWrapper.class));
        verify(cartServiceImpl, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("测试6: 增加购物车数量 - 菜品已下架抛出异常")
    void testAddQuantity_DishOffline_ThrowsException() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(103L);

        Dish offlineDish = Dish.builder()
                .id(103L)
                .dishName("下架菜品")
                .price(new BigDecimal("20.00"))
                .status(0)
                .build();

        when(dishMapper.selectById(103L)).thenReturn(offlineDish);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addQuantity(operationDTO);
        });

        assertEquals("该菜品不存在或已下架", exception.getMessage());
        verify(dishMapper, times(1)).selectById(103L);
        verify(cartServiceImpl, never()).getOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试7: 减少商品数量 - 数量大于1")
    void testSubQuantity_QuantityGreaterThanOne() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(104L);

        Cart existingCart = Cart.builder()
                .id(10L)
                .customerId(1001L)
                .dishId(104L)
                .addedPrice(new BigDecimal("28.00"))
                .quantity(3)
                .build();

        Dish dish = Dish.builder()
                .id(104L)
                .dishName("糖醋里脊")
                .price(new BigDecimal("30.00"))
                .status(1)
                .build();

        doReturn(existingCart).when(cartServiceImpl).getOne(any(LambdaQueryWrapper.class));
        when(dishMapper.selectById(104L)).thenReturn(dish);
        doReturn(true).when(cartServiceImpl).updateById(any(Cart.class));
        when(cartMapper.getCartDetailsByCustomerId(1001L)).thenReturn(Arrays.asList());

        List<CartVO> result = cartService.subQuantity(operationDTO);

        assertNotNull(result);
        verify(cartServiceImpl, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(cartServiceImpl, times(1)).updateById(argThat(cart -> 
            cart != null &&
            cart.getId().equals(10L) &&
            cart.getQuantity() == 2 &&
            cart.getAddedPrice().compareTo(new BigDecimal("30.00")) == 0
        ));
    }

    @Test
    @DisplayName("测试8: 减少商品数量 - 数量减到1后移除商品")
    void testSubQuantity_QuantityBecomesZero_RemoveItem() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(105L);

        Cart existingCart = Cart.builder()
                .id(15L)
                .customerId(1001L)
                .dishId(105L)
                .addedPrice(new BigDecimal("15.00"))
                .quantity(1)
                .build();

        doReturn(existingCart).when(cartServiceImpl).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(cartServiceImpl).removeById(15L);
        when(cartMapper.getCartDetailsByCustomerId(1001L)).thenReturn(Arrays.asList());

        List<CartVO> result = cartService.subQuantity(operationDTO);

        assertNotNull(result);
        verify(cartServiceImpl, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(cartServiceImpl, times(1)).removeById(15L);
        verify(cartServiceImpl, never()).updateById(any(Cart.class));
    }

    @Test
    @DisplayName("测试9: 减少商品数量 - 购物车无此商品抛出异常")
    void testSubQuantity_CartItemNotFound_ThrowsException() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(9999L);

        doReturn(null).when(cartServiceImpl).getOne(any(LambdaQueryWrapper.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.subQuantity(operationDTO);
        });

        assertEquals("购物车内无该菜品，无法减少数量", exception.getMessage());
        verify(cartServiceImpl, times(1)).getOne(any(LambdaQueryWrapper.class));
        verify(cartServiceImpl, never()).removeById(anyLong());
        verify(cartServiceImpl, never()).updateById(any(Cart.class));
    }

    @Test
    @DisplayName("测试10: 减少商品数量 - 同步更新最新价格")
    void testSubQuantity_UpdatePriceWhenReducing() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(106L);

        Cart existingCart = Cart.builder()
                .id(20L)
                .customerId(1001L)
                .dishId(106L)
                .addedPrice(new BigDecimal("22.00"))
                .quantity(2)
                .build();

        Dish dish = Dish.builder()
                .id(106L)
                .dishName("回锅肉")
                .price(new BigDecimal("26.00"))
                .status(1)
                .build();

        doReturn(existingCart).when(cartServiceImpl).getOne(any(LambdaQueryWrapper.class));
        when(dishMapper.selectById(106L)).thenReturn(dish);
        doReturn(true).when(cartServiceImpl).updateById(any(Cart.class));
        when(cartMapper.getCartDetailsByCustomerId(1001L)).thenReturn(Arrays.asList());

        List<CartVO> result = cartService.subQuantity(operationDTO);

        verify(cartServiceImpl, times(1)).updateById(argThat(cart -> 
            cart != null &&
            cart.getQuantity() == 1 &&
            cart.getAddedPrice().compareTo(new BigDecimal("26.00")) == 0
        ));
    }

    @Test
    @DisplayName("测试11: 减少商品数量 - 菜品查询失败时不更新价格")
    void testSubQuantity_DishQueryFails_SkipPriceUpdate() {
        CartOperationDTO operationDTO = new CartOperationDTO();
        operationDTO.setCustomerId(1001L);
        operationDTO.setDishId(107L);

        Cart existingCart = Cart.builder()
                .id(25L)
                .customerId(1001L)
                .dishId(107L)
                .addedPrice(new BigDecimal("19.00"))
                .quantity(2)
                .build();

        doReturn(existingCart).when(cartServiceImpl).getOne(any(LambdaQueryWrapper.class));
        when(dishMapper.selectById(107L)).thenReturn(null);
        doReturn(true).when(cartServiceImpl).updateById(any(Cart.class));
        when(cartMapper.getCartDetailsByCustomerId(1001L)).thenReturn(Arrays.asList());

        List<CartVO> result = cartService.subQuantity(operationDTO);

        verify(cartServiceImpl, times(1)).updateById(argThat(cart -> 
            cart != null &&
            cart.getQuantity() == 1 &&
            cart.getAddedPrice().compareTo(new BigDecimal("19.00")) == 0
        ));
    }
}
