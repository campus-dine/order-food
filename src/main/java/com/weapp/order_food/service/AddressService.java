package com.weapp.order_food.service;

import com.weapp.order_food.model.dto.AddressQueryDTO;
import com.weapp.order_food.model.dto.AddressSaveDTO;
import com.weapp.order_food.model.vo.AddressVO;

import java.util.List;

public interface AddressService {
    List<AddressVO> getAddressListByCustomerId(AddressQueryDTO dto);
    void saveAddress(AddressSaveDTO dto);
    void updateAddress(AddressSaveDTO dto);
    void toggleAddressDefault(AddressSaveDTO dto);

}
