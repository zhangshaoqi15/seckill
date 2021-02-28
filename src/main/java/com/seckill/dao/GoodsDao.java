package com.seckill.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.seckill.domain.SeckillGoods;
import com.seckill.vo.GoodsVo;

@Mapper
public interface GoodsDao {
	@Select("select g.*, sg.stock_count, sg.start_date, sg.end_date, sg.seckill_price "
			+ "from seckill_goods sg "
			+ "left join goods g on sg.goods_id = g.id")
	public List<GoodsVo> getGoodsVo();

	@Select("select g.*, sg.stock_count, sg.start_date, sg.end_date, sg.seckill_price "
			+ "from seckill_goods sg "
			+ "left join goods g on sg.goods_id = g.id "
			+ "where g.id = #{goodsId}" )
	public GoodsVo getGoodsVoById(@Param("goodsId") long goodsId);

	@Update("update seckill_goods set stock_count = stock_count-1 "
			+ "where goods_id = #{goodsId} and stock_count > 0 ")
	public int reduceStock(SeckillGoods g);

	@Update("update seckill_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
	public void resetStock(SeckillGoods g);

}
