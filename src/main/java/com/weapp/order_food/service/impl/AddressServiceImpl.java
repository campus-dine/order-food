package com.weapp.order_food.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weapp.order_food.entity.CustomerAddress;
import com.weapp.order_food.mapper.AddressMapper;
import com.weapp.order_food.model.dto.AddressQueryDTO;
import com.weapp.order_food.model.dto.AddressSaveDTO;
import com.weapp.order_food.model.vo.AddressVO;
import com.weapp.order_food.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressMapper, CustomerAddress> implements AddressService {

    @Override
    public List<AddressVO> getAddressListByCustomerId(AddressQueryDTO dto) {
        log.info("地址模块：正在获取用户 {} 的全部外送地址", dto.getCustomerId());

        // 1. 从数据库捞出所有的地址
        List<CustomerAddress> addresses = this.list(
                new LambdaQueryWrapper<CustomerAddress>()
                        .eq(CustomerAddress::getCustomerId, dto.getCustomerId())
                        .orderByDesc(CustomerAddress::getIsDefault) // 让常用的排在最上面
        );

        // 2. 打包转换成 VO 列表返还
        List<AddressVO> voList = new ArrayList<>();
        for (CustomerAddress addr : addresses) {
            AddressVO vo = new AddressVO();
            vo.setId(addr.getId());
            vo.setCustomerId(addr.getCustomerId());
            vo.setDetailAddress(addr.getDetailAddress());
            // 🚨 迎合前端：如果数据库是 NULL，VO里返还 0 给前端
            vo.setIsDefault(addr.getIsDefault() == null ? 0 : 1);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAddress(AddressSaveDTO dto) {
        log.info("地址模块：用户 {} 正在新增地址", dto.getCustomerId());

        // 🚨 修正 1：直接用安全对比，不需要写冗长的 != null 拼接，既防空指针又干净
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            demoteOldDefaultAddress(dto.getCustomerId());
        }

        CustomerAddress newAddr = CustomerAddress.builder()
                .customerId(dto.getCustomerId())
                .detailAddress(dto.getDetailAddress())
                // 🚨 你的安全改动，完美保留：
                .isDefault(Integer.valueOf(1).equals(dto.getIsDefault()) ? 1 : null)
                .build();

        this.save(newAddr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(AddressSaveDTO dto) {
        log.info("地址模块：用户 {} 正在修改地址 {}", dto.getCustomerId(), dto.getId());

        // 🚨 同样的防空指针全安全写法：
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            demoteOldDefaultAddress(dto.getCustomerId());
        }

        CustomerAddress updated = CustomerAddress.builder()
                .id(dto.getId())
                .customerId(dto.getCustomerId())
                .detailAddress(dto.getDetailAddress())
                .isDefault(Integer.valueOf(1).equals(dto.getIsDefault()) ? 1 : null)
                .build();

        this.updateById(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleAddressDefault(AddressSaveDTO dto) {
        log.info("地址模块：用户 {} 正在切换常用状态，目标地址 {}", dto.getCustomerId(), dto.getId());
        CustomerAddress address = this.getById(dto.getId());
        if (address == null) {
            throw new RuntimeException("要操作的地址不存在");
        }


        if (dto.getIsDefault() == 1) {
            // 设为常用：先洗掉旧的，再更新当前
            demoteOldDefaultAddress(dto.getCustomerId());
            this.update(new LambdaUpdateWrapper<CustomerAddress>()
                    .eq(CustomerAddress::getId, dto.getId())
                    .set(CustomerAddress::getIsDefault, 1));
        } else {
            // 取消常用：直接把当前的 1 洗成 NULL
            this.update(new LambdaUpdateWrapper<CustomerAddress>()
                    .eq(CustomerAddress::getId, dto.getId())
                    .set(CustomerAddress::getIsDefault, null));
        }
    }

    /**
     * 🚨 核心辅助方法：把该用户当前唯一的常用地址（1）强行降级为普通地址（NULL）
     */
    public void demoteOldDefaultAddress(Long customerId) {
        this.update(new LambdaUpdateWrapper<CustomerAddress>()
                .eq(CustomerAddress::getCustomerId, customerId)
                .eq(CustomerAddress::getIsDefault, 1)
                .set(CustomerAddress::getIsDefault, null) // 1 变 NULL，完美释放唯一索引位
        );
    }
}