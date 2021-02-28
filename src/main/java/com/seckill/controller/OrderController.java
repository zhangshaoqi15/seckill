package com.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.seckill.result.CodeMsg;
import com.seckill.result.Result;
import com.seckill.service.GoodsService;
import com.seckill.service.OrderService;
import com.seckill.vo.GoodsVo;
import com.seckill.vo.OrderDetailVo;
import com.seckill.domain.OrderInfo;
import com.seckill.domain.SeckillUser;
import com.seckill.redis.RedisService;

@Controller
@RequestMapping("/order")
public class OrderController {
	@Autowired
	RedisService redisService;
	@Autowired
	OrderService orderService;
	@Autowired
	GoodsService goodsService;

	@RequestMapping("/detail")
	@ResponseBody
	public Result<OrderDetailVo> detail(SeckillUser user, @RequestParam("orderId")long orderId ) {
		if(user == null) {
			return Result.secError(CodeMsg.SESSION_ERROR);
		}
		OrderInfo order = orderService.getOrderById(orderId);
		if(order == null) {
			return Result.secError(CodeMsg.ORDER_NOT_EXIST);
		}
		long goodsId = order.getGoodsId();
		GoodsVo goodsVo = goodsService.getGoodsVoById(goodsId);
		OrderDetailVo detailVo = new OrderDetailVo();
		detailVo.setGoods(goodsVo);
		detailVo.setOrder(order);
		
		return Result.success(detailVo);
	}

}