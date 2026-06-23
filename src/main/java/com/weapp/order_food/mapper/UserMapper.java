package com.weapp.order_food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weapp.order_food.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {


    @Select("select id from users where open_id = #{openId}")
    Long getUserByOpenId(String openId);

    // 新增用户 并返回id
    void insertUsers(User user);
}
