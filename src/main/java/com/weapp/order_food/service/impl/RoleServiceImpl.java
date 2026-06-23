package com.weapp.order_food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weapp.order_food.entity.Customer;
import com.weapp.order_food.entity.Merchant;
import com.weapp.order_food.mapper.CustomerMapper;
import com.weapp.order_food.mapper.MerchantMapper;
import com.weapp.order_food.service.RoleService;
import com.weapp.order_food.service.UserService;
import com.weapp.order_food.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final UserService userService; // 注入主管登录的 UserService
    private final CustomerMapper customerMapper;
    private final MerchantMapper merchantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 🚨 事务留在这里！保护整个多表开户流程
    public com.weapp.order_food.utils.Result<String> chooseUserRole(Long userId, Integer role) {
        log.info("身份聚合Service：开始为用户 {} 办理身份绑定...", userId);

        // 1. 调用主管登录的 UserService 去修改主表状态
        if (userService.updateUserRole(userId, role).getCode() != 200) {
            return Result.error("用户不存在，身份选择失败");
        }

        // 2. 各自领域的业务各自处理，彼此互不干扰
        if (role == 0) {
            // 客户/学生领域逻辑
            initCustomer(userId);
        } else if (role == 1) {
            // 商家领域逻辑
            initMerchant(userId);
        }

        return Result.success("用户身份选择并初始化成功");
    }

    private void initCustomer(Long userId) {
        Long count = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>().eq(Customer::getUserId, userId)
        );
        if (count == 0) {
            Customer customer = Customer.builder()
                    .userId(userId.intValue())
                    .balance(BigDecimal.ZERO)
                    .build();
            customerMapper.insert(customer);
            log.info("【客户领域】成功为用户 {} 初始化客户账户", userId);
        }
    }

    private void initMerchant(Long userId) {
        Long count = merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId)
        );
        if (count == 0) {
            Merchant merchant = Merchant.builder()
                    .userId(userId.intValue())
                    .balance(BigDecimal.ZERO)
                    .build();
            merchantMapper.insert(merchant);
            log.info("【商家领域】成功为用户 {} 初始化商家账户", userId);
        }
    }
}