package com.seckill.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.seckill.service.GoodsService;
import com.seckill.service.SeckillUserService;
import com.seckill.vo.GoodsDetailVo;
import com.seckill.vo.GoodsVo;
import com.seckill.domain.SeckillUser;
import com.seckill.redis.GoodsKey;
import com.seckill.redis.RedisService;
import com.seckill.result.Result;


@Controller
@RequestMapping("/goods")
public class GoodsController {
	@Autowired
	RedisService redisService;
	@Autowired
	SeckillUserService seckillUserService;
	@Autowired
	GoodsService goodsService;
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	@Autowired
	ApplicationContext applicationContext;
	
	/**
	 *查询商品列表
	 */
	@RequestMapping(value="/to_list", produces="text/html")
	@ResponseBody
	public String toList(HttpServletRequest request, HttpServletResponse response, 
				Model model, SeckillUser user) {
		model.addAttribute("user", user);
		//取缓存
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		
		if(user == null) {
			return "login";
		}
		List<GoodsVo> goodsList = goodsService.getGoodsVo();
		model.addAttribute("goodsList", goodsList);

		//如果为空，则手动渲染
		SpringWebContext ctx = new SpringWebContext(
				request, response, request.getServletContext(), 
				request.getLocale(), model.asMap(), applicationContext);
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		
		return html;
		
		//return "goods_list";
	}


	/**
	 * 打开详情页
	 * 通过 @PathVariable 可以将URL中占位符参数{xxx}绑定到处理器类的方法形参中@PathVariable("xxx")
	 */
	@RequestMapping(value="/to_detail/{goodsId}", produces="text/html")
	@ResponseBody
	public String toDetail(HttpServletRequest request, HttpServletResponse response, 
			Model model, SeckillUser user, @PathVariable("goodsId") long goodsId) {
		model.addAttribute("user", user);
		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		model.addAttribute("goods", goods);
		
		//取缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		
		long start = goods.getStartDate().getTime();
		long end = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		int seckillStatus = 0;
		int remainSeconds = 0;
		
		if(now < start) {	//秒杀还没开始
			seckillStatus = 0;
			remainSeconds = (int)((start-now)/1000);
		}
		else if(now > end){		//秒杀已经结束
			seckillStatus = 2;
			remainSeconds = -1;
		}
		else {	//秒杀中
			seckillStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("seckillStatus", seckillStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		
		// 如果为空，则手动渲染
		SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(),
				model.asMap(), applicationContext);
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
		if (!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		
		return html;
		
		//return "goods_detail";
	}
	
	/**
	 * 打开详情页（页面静态化版本）
	 */
	@RequestMapping(value="/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> toDetail2(HttpServletRequest request, HttpServletResponse response, 
			Model model, SeckillUser user, @PathVariable("goodsId") long goodsId) {

		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		long start = goods.getStartDate().getTime();
		long end = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		int seckillStatus = 0;
		int remainSeconds = 0;
		
		if(now < start) {	//秒杀还没开始
			seckillStatus = 0;
			remainSeconds = (int)((start-now)/1000);
		}
		else if(now > end){		//秒杀已经结束
			seckillStatus = 2;
			remainSeconds = -1;
		}
		else {	//秒杀中
			seckillStatus = 1;
			remainSeconds = 0;
		}
		
		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoods(goods);
		vo.setUser(user);
    	vo.setRemainSeconds(remainSeconds);
    	vo.setSeckillStatus(seckillStatus);
		
		return Result.success(vo);
		
		//return "goods_detail";
	}
}
