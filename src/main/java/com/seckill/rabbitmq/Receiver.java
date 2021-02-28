package com.seckill.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.seckill.domain.SeckillOrder;
import com.seckill.domain.SeckillUser;
import com.seckill.redis.RedisService;
import com.seckill.service.GoodsService;
import com.seckill.service.OrderService;
import com.seckill.service.SeckillService;
import com.seckill.vo.GoodsVo;

@Component
public class Receiver {
	private static Logger log = LoggerFactory.getLogger(Receiver.class);
	
	@Autowired
	AmqpTemplate amqpTemplate;
	@Autowired
	GoodsService goodsService;
	@Autowired
	OrderService orderService;
	@Autowired
	SeckillService seckillService;
	@Autowired
	RedisService redisService;
	
	@RabbitListener(queues=MQConfig.SECKILL_DIRECT_QUEUE)
	public void receive(String message) {
		log.info("receive msg: "+message);
		SeckillMessage skmsg = RedisService.stringToBean(message, SeckillMessage.class);
		SeckillUser user = skmsg.getUser();
		long goodsId = skmsg.getGoodsId();
		
		GoodsVo goods = goodsService.getGoodsVoById(goodsId);
		int stock = goods.getStockCount();
		if(stock <= 0) {	//库存不足，秒杀失败
			return;
		}
		
		SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
		if (order != null) {	//重复秒杀
			return;
		}
		
		seckillService.seckill(user, goods);
	}
	
	//@RabbitListener指定从MQConfig.QUEUE中消费数据
	/*
	@RabbitListener(queues=MQConfig.DIRECT_QUEUE)
	public void receive(String message) {
		log.info("receive msg: "+message);
	}
	
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
	public void receiveTopic1(String message) {
		log.info("receive queue1 msg: "+message);
	}
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String message) {
		log.info("receive queue2 msg: "+message);
	}
	
	@RabbitListener(queues=MQConfig.HEADERS_QUEUE)
	public void receiveHeaders(byte[] message) {
		log.info("receive headers queue msg: "+new String(message));
	}
	*/
}
