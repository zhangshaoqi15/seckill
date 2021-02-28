package com.seckill.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.seckill.domain.OrderInfo;
import com.seckill.domain.SeckillOrder;
import com.seckill.domain.SeckillUser;
import com.seckill.redis.RedisService;
import com.seckill.redis.SeckillKey;
import com.seckill.util.MD5util;
import com.seckill.util.UUIDUtil;
import com.seckill.vo.GoodsVo;

@Service
public class SeckillService {

	@Autowired
	GoodsService goodsService;
	@Autowired
	OrderService orderService;
	@Autowired
	RedisService redisService;

	/**
	 * 减去库存，编写订单，写入秒杀订单，封装成事务
	 */
	@Transactional
	public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
		boolean isSuccess = goodsService.reduceStock(goods);
		if(isSuccess) {
			return orderService.createOrder(user, goods);
		}else {
			setGoodsOver(goods.getId());
			return null;
		}
	}
	
	/**
	 * 检测秒杀是否成功（加载进redis中），或失败，或轮询中
	 */
	public long getSeckillResult(Long userId, long goodsId) {
		SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(userId, goodsId);
		if(order != null) {
			return order.getOrderId();
		}
		else {
			boolean isOver = getGoodsOver(goodsId);
			if(isOver) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}
	
	private void setGoodsOver(Long goodsId) {
		redisService.set(SeckillKey.isGoodsOver, ""+goodsId, true);
	}
	
	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(SeckillKey.isGoodsOver, ""+goodsId);
	}
	
	/**
	 * UUID生成随机数，MD5加密，拼接成秒杀地址的url的一部分
	 */
	public String createPath(SeckillUser user, long goodsId) {
		String str = MD5util.MD5(UUIDUtil.uuid()+"123");
		redisService.set(SeckillKey.getSeckillPath, ""+user.getId()+"_"+goodsId, str);
		return str;
	}

	/**
	 *验证传入的path和缓存的path是否相等
	 */
	public boolean checkPath(SeckillUser user, long goodsId, String path) {
		if(user == null || path == null) {
			return false;
		}
		String oldPath = redisService.get(SeckillKey.getSeckillPath, ""+user.getId()+"_"+goodsId, String.class);
		return path.equals(oldPath);
	}
	
	/**
	 *创建验证码
	 */
	public BufferedImage createVerifyCode(SeckillUser user, long goodsId) {
		if(user == null || goodsId <= 0) {
			return null;
		}
		
		int width = 80;
		int height = 32;
		//create the image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		//列出验证码（算式）
		String verifyCode = generateVerifyCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		//利用ScriptEngine计算验证码
		int rnd = calc(verifyCode);
		redisService.set(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId, rnd);
		//输出图片	
		return image;
	}
	
	private String generateVerifyCode(Random rdm) {
		char[] ops = new char[] {'+', '-', '*'};
		int num1 = rdm.nextInt(10);
	    int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		String exp = ""+ num1 + op1 + num2 + op2 + num3;
		return exp;
	}
	
	private int calc(String exp) {
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return (Integer)engine.eval(exp);
		}
		catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 *验证验证码是否正确
	 */
	public boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
		if(user == null || goodsId <= 0) {
			return false;
		}
		Integer oldCode = redisService.get(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId, Integer.class);
		
		if(oldCode == null || verifyCode != oldCode) {
			return false;
		}
		redisService.delete(SeckillKey.getSeckillVerifyCode, user.getId()+","+goodsId);
		
		return true;
	}

	/**
	 *测试用：重置数据库的数据
	 */
	public void reset(List<GoodsVo> goodsList) {
		goodsService.resetStock(goodsList);
		orderService.deleteOrders();
	}

}
