package com.smilelive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smilelive.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {
    @Select("select wallet from user where id=#{id}")
    Integer getWalletById(Long id);
}
