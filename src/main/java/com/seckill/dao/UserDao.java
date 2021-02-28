package com.seckill.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.seckill.domain.User;

@Mapper
public interface UserDao {
	
	@Select("select * from user where id = #{id}")
	public User getById(@Param("id") long id);
	
	@Insert("insert into user(id, name) values(#{id}, #{name})")
	public int insert(User user);

}
