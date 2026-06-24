package com.weapp.order_food.controller;


import com.weapp.order_food.model.dto.AddressQueryDTO;
import com.weapp.order_food.model.dto.AddressSaveDTO;
import com.weapp.order_food.model.vo.AddressVO;
import com.weapp.order_food.service.AddressService;
import com.weapp.order_food.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "客户外送地址模块")
@RestController
@RequestMapping("/api/customer/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @ApiOperation("查询该用户所有的地址列表")
    @GetMapping("/list")
    public Result<List<AddressVO>> getAddressList(AddressQueryDTO dto) {
        if (dto.getCustomerId() == null) {
            return Result.error("客户ID不能为空");
        }
        List<AddressVO> list = addressService.getAddressListByCustomerId(dto);
        return Result.success(list);
    }

    @ApiOperation("新增外送地址")
    @PostMapping("/add")
    public Result<String> addAddress(@RequestBody AddressSaveDTO dto) {
        if (dto.getCustomerId() == null || dto.getDetailAddress() == null) {
            return Result.error("参数不完整");
        }
        addressService.saveAddress(dto);
        return Result.success("success");
    }

    @ApiOperation("修改外送地址")
    @PutMapping("/update")
    public Result<String> updateAddress(@RequestBody AddressSaveDTO dto) {
        if (dto.getId() == null || dto.getCustomerId() == null) {
            return Result.error("缺少修改目标主键");
        }
        addressService.updateAddress(dto);
        return Result.success("success");
    }

    @ApiOperation("设置或取消常用地址")
    @PutMapping("/toggle-default")
    public Result<String> toggleDefault(@RequestBody AddressSaveDTO dto) {
        if (dto.getId() == null || dto.getCustomerId() == null || dto.getIsDefault() == null) {
            return Result.error("参数不完整");
        }
        addressService.toggleAddressDefault(dto);
        return Result.success("success");
    }
}