package com.seckill.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.seckill.access.UserContext;
import com.seckill.domain.SeckillUser;
import com.seckill.service.SeckillUserService;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
	@Autowired
	SeckillUserService seckillUserService;

	/**
	 *用于判定是否需要处理该参数分解，返回true为需要，并会去调用下面的方法resolveArgument。
	 *判断如果参数类型为SeckillUserService才处理
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz==SeckillUser.class;
	}

	/**
	 *真正用于处理参数分解的方法，返回的Object就是controller方法上的形参对象。
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);		
		String paramToken = request.getParameter(seckillUserService.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(request, seckillUserService.COOKIE_NAME_TOKEN);
		
		return UserContext.getUser();
		/*
		//防止有时候token不在cookie的情况，需要用request获取，并选取其中一个参数
		if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
			return null;
		}
		String token = StringUtils.isEmpty(cookieToken)? paramToken: cookieToken;
		
		return seckillUserService.getByToken(response, token);
		*/
	}

	private String getCookieValue(HttpServletRequest request, String cookie) {
		Cookie[] cookies = request.getCookies();
		if(cookies == null || cookies.length <= 0) {
			return null;
		}
		for(Cookie ck : cookies) {
			if(ck.getName().equals(cookie)) {
				return ck.getValue();
			}
		}
		
		return null;
	}

}
