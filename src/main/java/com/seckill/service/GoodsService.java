package com.seckill.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.seckill.dao.GoodsDao;
import com.seckill.domain.SeckillGoods;
import com.seckill.vo.GoodsVo;

@Service
public class GoodsService {
	@Autowired
	GoodsDao goodsDao;
	
	public List<GoodsVo> getGoodsVo() {
		return goodsDao.getGoodsVo();
	}
	
	public GoodsVo getGoodsVoById(long goodsId) {
		return goodsDao.getGoodsVoById(goodsId);
	}

	public boolean reduceStock(GoodsVo goods) {
		SeckillGoods g = new SeckillGoods();
		g.setGoodsId(goods.getId());
		int ret = goodsDao.reduceStock(g);
		return ret > 0;
	}

	/**
	 * 测试用
	 */
	public void resetStock(List<GoodsVo> goodsList) {
		for(GoodsVo goods : goodsList ) {
			SeckillGoods g = new SeckillGoods();
			g.setGoodsId(goods.getId());
			g.setStockCount(goods.getStockCount());
			goodsDao.resetStock(g);
		}
	}
}
