package com.seckill.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.seckill.access.AccessLimit;
import com.seckill.domain.SeckillOrder;
import com.seckill.domain.SeckillUser;
import com.seckill.rabbitmq.SeckillMessage;
import com.seckill.rabbitmq.Sender;
import com.seckill.redis.GoodsKey;
import com.seckill.redis.OrderKey;
import com.seckill.redis.RedisService;
import com.seckill.redis.SeckillKey;
import com.seckill.result.CodeMsg;
import com.seckill.result.Result;
import com.seckill.service.GoodsService;
import com.seckill.service.OrderService;
import com.seckill.service.SeckillService;
import com.seckill.vo.GoodsVo;



@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean{
	@Autowired
	GoodsService goodsService;
	@Autowired
	OrderService orderService;
	@Autowired
	SeckillService seckillService;
	@Autowired
	RedisService redisService;
	@Autowired
	Sender sender;
	
	private Map<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();

	/**
	 *系统初始化
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.getGoodsVo();
		if(goodsList == null) {
			return ;
		}
		for(GoodsVo good: goodsList) {
			redisService.set(GoodsKey.getSeckillGoodsStock, ""+good.getId(), good.getStockCount());
			localOverMap.put(good.getId(), false);
		}
	}
	
	/**
	 *秒杀功能（页面静态化版本）
	 */
	@RequestMapping(value="/{path}/do_seckill", method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> seckill(Model model, SeckillUser user, 
			@RequestParam("goodsId") long goodsId, 
			@PathVariable("path")String path ) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.secError(CodeMsg.SESSION_ERROR);
		}
		
		//验证path
		boolean isTrue = seckillService.checkPath(user, goodsId, path);
		if(!isTrue) {
			return Result.secError(CodeMsg.REQUEST_ILLEGAL); 
		}
		
		//设置标记，表示该商品秒杀条件不满足，不需要访问redis
		boolean isOver = localOverMap.get(goodsId);
		if(isOver) {
			return Result.secError(CodeMsg.SECKILL_OVER);
		}
		
		long stock = redisService.decr(GoodsKey.getSeckillGoodsStock, ""+goodsId);
		if(stock < 0) {	//库存不足，秒杀失败
			localOverMap.put(goodsId, true);
			return Result.secError(CodeMsg.SECKILL_OVER);
		}
		
		SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
		if (order != null) {	//重复秒杀
			return Result.secError(CodeMsg.REPEATE_SECKILL);
		}
		
		//入队
		SeckillMessage skmsg = new SeckillMessage();
		skmsg.setUser(user);
		skmsg.setGoodsId(goodsId);
		sender.sendSeckillMsg(skmsg);
		
		return Result.success(0); //0代表正在排队
		
		/*
		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		int stock = goods.getStockCount();
		if(stock <= 0) {	//库存不足，秒杀失败
			return Result.secError(CodeMsg.SECKILL_OVER);
		}
		
		SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
		if (order != null) {	//重复秒杀
			return Result.secError(CodeMsg.REPEATE_SECKILL);
		}
		
		OrderInfo orderInfo = seckillService.seckill(user, goods);
		
		return Result.success(orderInfo);
		*/
	}
	
	/**
	 *客户端的轮询结果
	 *result为orderID代表成功
	 * -1代表失败
	 * 0代表排队中
	 */
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> seckillResult(Model model, SeckillUser user, @RequestParam("goodsId") long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.secError(CodeMsg.SESSION_ERROR);
		}
		long result = seckillService.getSeckillResult(user.getId(), goodsId);
		System.out.println("【RESULT】: "+result);
		return Result.success(result);
	}
	
	/**
	 *获取秒杀地址
	 */
	@AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath( SeckillUser user, 
    		@RequestParam("goodsId")long goodsId, 
    		@RequestParam("verifyCode")int verifyCode) {
    	
    	if(user == null) {
    		return Result.secError(CodeMsg.SESSION_ERROR);
    	}
    	
    	boolean isVerifyCode = seckillService.checkVerifyCode(user, goodsId, verifyCode);
    	if(!isVerifyCode) {
    		return Result.secError(CodeMsg.VERIFYCODE_ERROR);
    	}
    	
    	String path = seckillService.createPath(user, goodsId);
    	return Result.success(path);
    }
    
	/**
	 *获取验证码
	 */
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(HttpServletResponse response, SeckillUser user, long goodsId) {
    	if(user == null) {
    		return Result.secError(CodeMsg.SESSION_ERROR);
    	}
    	
    	BufferedImage image = seckillService.createVerifyCode(user, goodsId);
    	try {
    		OutputStream out = response.getOutputStream();
    		ImageIO.write(image, "JPEG", out);
    		out.flush();
    		out.close();
    		return null;
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		return Result.secError(CodeMsg.SECKILL_FAILED);
    	}
    	
    }
	
	/**
	 *测试用：重置数据库和缓存的数据
	 */
	@RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
		List<GoodsVo> goodsList = goodsService.getGoodsVo();
		for(GoodsVo goods : goodsList) {
			goods.setStockCount(10);
			redisService.set(GoodsKey.getSeckillGoodsStock, ""+goods.getId(), 10);
			localOverMap.put(goods.getId(), false);
		}
		redisService.delete(OrderKey.getSeckillOrderByUidGid);
		redisService.delete(SeckillKey.isGoodsOver);
		seckillService.reset(goodsList);
		return Result.success(true);
	}
	
}
