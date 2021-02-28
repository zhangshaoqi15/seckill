package com.seckill.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.seckill.dao.SeckillUserDao;
import com.seckill.domain.SeckillUser;
import com.seckill.exception.GlobalException;
import com.seckill.redis.RedisService;
import com.seckill.redis.SeckillUserKey;
import com.seckill.result.CodeMsg;
import com.seckill.util.MD5util;
import com.seckill.util.UUIDUtil;
import com.seckill.vo.LoginVo;

@Service
public class SeckillUserService {
	//cookie中的暂时的token name
	public static final String COOKIE_NAME_TOKEN = "token";
	
	@Autowired
	SeckillUserDao seckillUserDao;
	@Autowired
	RedisService redisService;
	
	/*
	 * 通过ID获取seckilluser
	 */
	public SeckillUser getById(long id) {
		//取缓存
		SeckillUser user = redisService.get(SeckillUserKey.getById, ""+id, SeckillUser.class);
		if(user != null) {
			return user;
		}
		//取数据库
		user = seckillUserDao.getById(id);
		if(user != null) {
			redisService.set(SeckillUserKey.getById, ""+id, user);
		}
		
		return user;
	}
	
	/*
	 * 通过Token获取seckilluser
	 */
	public SeckillUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		SeckillUser user = redisService.get(SeckillUserKey.token, token, SeckillUser.class);
		//延迟cookie有效期，从最后一次访问开始重新计算
		if(user != null) {
			addCookie(response, user, token);
		}
		
		return user;
	}
	
	/*
	 * 修改密码
	 */
	public boolean updatePassword(String token, long id, String newPassword) {
		SeckillUser user = getById(id);
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//更新数据库
		SeckillUser toBeUpdate = new SeckillUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5util.DBPass(newPassword, user.getSalt()));
		seckillUserDao.update(toBeUpdate);
		//更新缓存
		redisService.delete(SeckillUserKey.getById, ""+id);
		user.setPassword(toBeUpdate.getPassword());
		redisService.set(SeckillUserKey.token, token, user);
		return true;
	}

	/*
	 * 登录验证
	 */
	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		//服务器错误
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		SeckillUser user = getById(Long.parseLong(mobile));
		
		//手机号不存在
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		
		String DBPass = user.getPassword();
		String DBSalt = user.getSalt();
		String result = MD5util.DBPass(formPass, DBSalt);
		
		//密码错误
		if(!result.equals(DBPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		String token = UUIDUtil.uuid();
		addCookie(response, user, token);
		
		return true;
	}

	/*
	 * 生成cookie
	 */
	private void addCookie(HttpServletResponse response, SeckillUser user, String token) {
		//token是用户ID，标识每一个用户，存放到redis中
		redisService.set(SeckillUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
		cookie.setMaxAge(300);//cookie有效期设为5分钟
		cookie.setPath("/");
		response.addCookie(cookie);
	}

}
