package com.weapp.order_food.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weapp.order_food.model.dto.AddressQueryDTO;
import com.weapp.order_food.model.dto.AddressSaveDTO;
import com.weapp.order_food.model.vo.AddressVO;
import com.weapp.order_food.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@DisplayName("地址控制器测试 - API接口验证")
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @Autowired
    private ObjectMapper objectMapper;

    private AddressQueryDTO queryDTO;
    private AddressSaveDTO saveDTO;
    private List<AddressVO> mockAddressList;

    @BeforeEach
    void setUp() {
        queryDTO = new AddressQueryDTO();
        queryDTO.setCustomerId(1001L);

        saveDTO = new AddressSaveDTO();
        saveDTO.setId(1L);
        saveDTO.setCustomerId(1001L);
        saveDTO.setDetailAddress("北京市朝阳区某某街道1号");
        saveDTO.setIsDefault(1);

        AddressVO vo1 = new AddressVO();
        vo1.setId(1L);
        vo1.setCustomerId(1001L);
        vo1.setDetailAddress("北京市朝阳区某某街道1号");
        vo1.setIsDefault(1);

        AddressVO vo2 = new AddressVO();
        vo2.setId(2L);
        vo2.setCustomerId(1001L);
        vo2.setDetailAddress("北京市海淀区某某街道2号");
        vo2.setIsDefault(0);

        mockAddressList = Arrays.asList(vo1, vo2);
    }

    @Test
    @DisplayName("测试1: 查询地址列表成功")
    void testGetAddressList_Success() throws Exception {
        when(addressService.getAddressListByCustomerId(any(AddressQueryDTO.class))).thenReturn(mockAddressList);

        mockMvc.perform(get("/api/customer/address/list")
                .param("customerId", "1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].detailAddress").value("北京市朝阳区某某街道1号"));

        verify(addressService, times(1)).getAddressListByCustomerId(any(AddressQueryDTO.class));
    }

    @Test
    @DisplayName("测试2: 查询地址列表失败 - 缺少customerId")
    void testGetAddressList_MissingCustomerId() throws Exception {
        mockMvc.perform(get("/api/customer/address/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("客户ID不能为空"));

        verify(addressService, never()).getAddressListByCustomerId(any());
    }

    @Test
    @DisplayName("测试3: 新增地址成功")
    void testAddAddress_Success() throws Exception {
        doNothing().when(addressService).saveAddress(any(AddressSaveDTO.class));

        mockMvc.perform(post("/api/customer/address/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(addressService, times(1)).saveAddress(any(AddressSaveDTO.class));
    }

    @Test
    @DisplayName("测试4: 新增地址失败 - 缺少customerId")
    void testAddAddress_MissingCustomerId() throws Exception {
        AddressSaveDTO incompleteDTO = new AddressSaveDTO();
        incompleteDTO.setDetailAddress("测试地址");

        mockMvc.perform(post("/api/customer/address/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("参数不完整"));

        verify(addressService, never()).saveAddress(any());
    }

    @Test
    @DisplayName("测试5: 修改地址成功")
    void testUpdateAddress_Success() throws Exception {
        doNothing().when(addressService).updateAddress(any(AddressSaveDTO.class));

        mockMvc.perform(put("/api/customer/address/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(addressService, times(1)).updateAddress(any(AddressSaveDTO.class));
    }

    @Test
    @DisplayName("测试6: 修改地址失败 - 缺少id")
    void testUpdateAddress_MissingId() throws Exception {
        AddressSaveDTO incompleteDTO = new AddressSaveDTO();
        incompleteDTO.setCustomerId(1001L);
        incompleteDTO.setDetailAddress("新地址");

        mockMvc.perform(put("/api/customer/address/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("缺少修改目标主键"));

        verify(addressService, never()).updateAddress(any());
    }

    @Test
    @DisplayName("测试7: 切换常用地址成功 - 设为常用")
    void testToggleDefault_SetAsDefault_Success() throws Exception {
        doNothing().when(addressService).toggleAddressDefault(any(AddressSaveDTO.class));

        mockMvc.perform(put("/api/customer/address/toggle-default")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(addressService, times(1)).toggleAddressDefault(any(AddressSaveDTO.class));
    }

    @Test
    @DisplayName("测试8: 切换常用地址失败 - 缺少isDefault")
    void testToggleDefault_MissingIsDefault() throws Exception {
        AddressSaveDTO incompleteDTO = new AddressSaveDTO();
        incompleteDTO.setId(1L);
        incompleteDTO.setCustomerId(1001L);

        mockMvc.perform(put("/api/customer/address/toggle-default")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("参数不完整"));

        verify(addressService, never()).toggleAddressDefault(any());
    }
}
