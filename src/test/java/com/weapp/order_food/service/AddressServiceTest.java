package com.weapp.order_food.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.weapp.order_food.entity.CustomerAddress;
import com.weapp.order_food.mapper.AddressMapper;
import com.weapp.order_food.model.dto.AddressQueryDTO;
import com.weapp.order_food.model.dto.AddressSaveDTO;
import com.weapp.order_food.model.vo.AddressVO;
import com.weapp.order_food.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("地址服务测试 - 地址管理与常用地址切换")
class AddressServiceTest {

    @Autowired
    private AddressService addressService;

    @SpyBean
    private AddressServiceImpl addressServiceImpl;

    @BeforeEach
    void setUp() {
        reset(addressServiceImpl);
    }

    // ==================== 查询地址列表测试 ====================

    @Test
    @DisplayName("测试1: 查询用户地址列表成功 - 包含常用地址")
    void testGetAddressListByCustomerId_WithDefaultAddress_Success() {
        Long customerId = 1001L;
        AddressQueryDTO queryDTO = new AddressQueryDTO();
        queryDTO.setCustomerId(customerId);

        CustomerAddress addr1 = CustomerAddress.builder()
                .id(1L)
                .customerId(customerId)
                .detailAddress("北京市朝阳区某某街道1号")
                .isDefault(1)
                .build();

        CustomerAddress addr2 = CustomerAddress.builder()
                .id(2L)
                .customerId(customerId)
                .detailAddress("北京市海淀区某某街道2号")
                .isDefault(null)
                .build();

        List<CustomerAddress> mockAddresses = Arrays.asList(addr1, addr2);

        doReturn(mockAddresses).when(addressServiceImpl).list(any(LambdaQueryWrapper.class));

        List<AddressVO> result = addressService.getAddressListByCustomerId(queryDTO);

        assertNotNull(result);
        assertEquals(2, result.size());

        AddressVO vo1 = result.get(0);
        assertEquals(1L, vo1.getId());
        assertEquals(customerId, vo1.getCustomerId());
        assertEquals("北京市朝阳区某某街道1号", vo1.getDetailAddress());
        assertEquals(1, vo1.getIsDefault());

        AddressVO vo2 = result.get(1);
        assertEquals(2L, vo2.getId());
        assertEquals(0, vo2.getIsDefault());

        verify(addressServiceImpl, times(1)).list(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试2: 查询用户地址列表成功 - 无地址")
    void testGetAddressListByCustomerId_NoAddresses_Success() {
        Long customerId = 9999L;
        AddressQueryDTO queryDTO = new AddressQueryDTO();
        queryDTO.setCustomerId(customerId);

        doReturn(Arrays.asList()).when(addressServiceImpl).list(any(LambdaQueryWrapper.class));

        List<AddressVO> result = addressService.getAddressListByCustomerId(queryDTO);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(addressServiceImpl, times(1)).list(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试3: 查询用户地址列表 - NULL isDefault 转换为 0")
    void testGetAddressListByCustomerId_NullToZeroConversion() {
        Long customerId = 1002L;
        AddressQueryDTO queryDTO = new AddressQueryDTO();
        queryDTO.setCustomerId(customerId);

        CustomerAddress addr = CustomerAddress.builder()
                .id(3L)
                .customerId(customerId)
                .detailAddress("上海市浦东新区某某路3号")
                .isDefault(null)
                .build();

        doReturn(Arrays.asList(addr)).when(addressServiceImpl).list(any(LambdaQueryWrapper.class));

        List<AddressVO> result = addressService.getAddressListByCustomerId(queryDTO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getIsDefault());
    }

    // ==================== 新增地址测试 ====================

    @Test
    @DisplayName("测试4: 新增普通地址成功 - 非默认地址")
    void testSaveAddress_NormalAddress_Success() {
        AddressSaveDTO saveDTO = new AddressSaveDTO();
        saveDTO.setCustomerId(1001L);
        saveDTO.setDetailAddress("广州市天河区某某大厦4号");
        saveDTO.setIsDefault(0);

        doNothing().when(addressServiceImpl).demoteOldDefaultAddress(anyLong());
        doReturn(true).when(addressServiceImpl).save(any(CustomerAddress.class));

        addressService.saveAddress(saveDTO);

        verify(addressServiceImpl, never()).demoteOldDefaultAddress(anyLong());
        verify(addressServiceImpl, times(1)).save(any(CustomerAddress.class));
    }

    @Test
    @DisplayName("测试5: 新增常用地址成功 - 自动降级旧常用地址")
    void testSaveAddress_DefaultAddress_DemoteOldAddress() {
        AddressSaveDTO saveDTO = new AddressSaveDTO();
        saveDTO.setCustomerId(1001L);
        saveDTO.setDetailAddress("深圳市南山区科技园5号");
        saveDTO.setIsDefault(1);

        doNothing().when(addressServiceImpl).demoteOldDefaultAddress(anyLong());
        doReturn(true).when(addressServiceImpl).save(any(CustomerAddress.class));

        addressService.saveAddress(saveDTO);

        verify(addressServiceImpl, times(1)).demoteOldDefaultAddress(1001L);
        verify(addressServiceImpl, times(1)).save(argThat(addr ->
                addr != null &&
                        addr.getCustomerId().equals(1001L) &&
                        "深圳市南山区科技园5号".equals(addr.getDetailAddress()) &&
                        Integer.valueOf(1).equals(addr.getIsDefault())
        ));
    }

    @Test
    @DisplayName("测试6: 新增地址 - isDefault 为 NULL 时不触发降级")
    void testSaveAddress_NullIsDefault_NoDemote() {
        AddressSaveDTO saveDTO = new AddressSaveDTO();
        saveDTO.setCustomerId(1001L);
        saveDTO.setDetailAddress("成都市武侯区某某街6号");
        saveDTO.setIsDefault(null);

        doReturn(true).when(addressServiceImpl).save(any(CustomerAddress.class));

        addressService.saveAddress(saveDTO);

        verify(addressServiceImpl, never()).demoteOldDefaultAddress(anyLong());
        verify(addressServiceImpl, times(1)).save(argThat(addr ->
                addr != null && addr.getIsDefault() == null
        ));
    }

    // ==================== 修改地址测试 ====================

    @Test
    @DisplayName("测试7: 修改地址成功 - 保持普通地址")
    void testUpdateAddress_NormalAddress_Success() {
        AddressSaveDTO updateDTO = new AddressSaveDTO();
        updateDTO.setId(1L);
        updateDTO.setCustomerId(1001L);
        updateDTO.setDetailAddress("杭州市西湖区新地址7号");
        updateDTO.setIsDefault(0);

        doReturn(true).when(addressServiceImpl).updateById(any(CustomerAddress.class));

        addressService.updateAddress(updateDTO);

        verify(addressServiceImpl, never()).demoteOldDefaultAddress(anyLong());
        verify(addressServiceImpl, times(1)).updateById(argThat(addr ->
                addr != null &&
                        addr.getId().equals(1L) &&
                        "杭州市西湖区新地址7号".equals(addr.getDetailAddress()) &&
                        addr.getIsDefault() == null
        ));
    }

    @Test
    @DisplayName("测试8: 修改地址成功 - 设为常用地址并降级旧的")
    void testUpdateAddress_SetAsDefault_DemoteOldAddress() {
        AddressSaveDTO updateDTO = new AddressSaveDTO();
        updateDTO.setId(2L);
        updateDTO.setCustomerId(1001L);
        updateDTO.setDetailAddress("南京市鼓楼区新地址8号");
        updateDTO.setIsDefault(1);

        doNothing().when(addressServiceImpl).demoteOldDefaultAddress(anyLong());
        doReturn(true).when(addressServiceImpl).updateById(any(CustomerAddress.class));

        addressService.updateAddress(updateDTO);

        verify(addressServiceImpl, times(1)).demoteOldDefaultAddress(1001L);
        verify(addressServiceImpl, times(1)).updateById(argThat(addr ->
                addr != null &&
                        addr.getId().equals(2L) &&
                        Integer.valueOf(1).equals(addr.getIsDefault())
        ));
    }

    // ==================== 切换常用状态测试 ====================

    @Test
    @DisplayName("测试9: 切换地址为常用 - 成功")
    void testToggleAddressDefault_SetAsDefault_Success() {
        AddressSaveDTO toggleDTO = new AddressSaveDTO();
        toggleDTO.setId(3L);
        toggleDTO.setCustomerId(1001L);
        toggleDTO.setIsDefault(1);

        CustomerAddress existingAddr = CustomerAddress.builder()
                .id(3L)
                .customerId(1001L)
                .detailAddress("武汉市江汉区某某路9号")
                .isDefault(null)
                .build();

        doReturn(existingAddr).when(addressServiceImpl).getById(3L);
        doNothing().when(addressServiceImpl).demoteOldDefaultAddress(anyLong());
        doReturn(true).when(addressServiceImpl).update(any(LambdaUpdateWrapper.class));

        addressService.toggleAddressDefault(toggleDTO);

        verify(addressServiceImpl, times(1)).getById(3L);
        verify(addressServiceImpl, times(1)).demoteOldDefaultAddress(1001L);
        verify(addressServiceImpl, times(1)).update(any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("测试10: 切换地址取消常用 - 成功")
    void testToggleAddressDefault_RemoveDefault_Success() {
        AddressSaveDTO toggleDTO = new AddressSaveDTO();
        toggleDTO.setId(1L);
        toggleDTO.setCustomerId(1001L);
        toggleDTO.setIsDefault(0);

        CustomerAddress existingAddr = CustomerAddress.builder()
                .id(1L)
                .customerId(1001L)
                .detailAddress("北京市朝阳区某某街道1号")
                .isDefault(1)
                .build();

        doReturn(existingAddr).when(addressServiceImpl).getById(1L);
        doReturn(true).when(addressServiceImpl).update(any(LambdaUpdateWrapper.class));

        addressService.toggleAddressDefault(toggleDTO);

        verify(addressServiceImpl, times(1)).getById(1L);
        verify(addressServiceImpl, never()).demoteOldDefaultAddress(anyLong());
        verify(addressServiceImpl, times(1)).update(any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("测试11: 切换常用状态 - 地址不存在抛出异常")
    void testToggleAddressDefault_AddressNotFound_ThrowsException() {
        AddressSaveDTO toggleDTO = new AddressSaveDTO();
        toggleDTO.setId(9999L);
        toggleDTO.setCustomerId(1001L);
        toggleDTO.setIsDefault(1);

        doReturn(null).when(addressServiceImpl).getById(9999L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            addressService.toggleAddressDefault(toggleDTO);
        });

        assertEquals("要操作的地址不存在", exception.getMessage());
        verify(addressServiceImpl, times(1)).getById(9999L);
        verify(addressServiceImpl, never()).demoteOldDefaultAddress(anyLong());
        verify(addressServiceImpl, never()).update(any(LambdaUpdateWrapper.class));
    }

    // ==================== 辅助方法测试 ====================

    @Test
    @DisplayName("测试12: demoteOldDefaultAddress - 降级旧常用地址")
    void testDemoteOldDefaultAddress_Success() {
        Long customerId = 1001L;

        doReturn(true).when(addressServiceImpl).update(any(LambdaUpdateWrapper.class));

        reflectivelyCallDemoteMethod(customerId);

        verify(addressServiceImpl, times(1)).update(any(LambdaUpdateWrapper.class));
    }

    /**
     * 通过反射调用私有方法 demoteOldDefaultAddress 进行测试
     */
    private void reflectivelyCallDemoteMethod(Long customerId) {
        try {
            java.lang.reflect.Method method = AddressServiceImpl.class.getDeclaredMethod(
                    "demoteOldDefaultAddress", Long.class
            );
            method.setAccessible(true);
            method.invoke(addressServiceImpl, customerId);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }
}
