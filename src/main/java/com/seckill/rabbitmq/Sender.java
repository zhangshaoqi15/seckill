package com.seckill.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.seckill.redis.RedisService;

@Service
public class Sender {
	private static Logger log = LoggerFactory.getLogger(Sender.class);

	@Autowired
	AmqpTemplate amqpTemplate;
	
	public void sendSeckillMsg(SeckillMessage skmsg) {
		String msg = RedisService.beanToString(skmsg);
		log.info("send msg: "+msg);
		amqpTemplate.convertAndSend(MQConfig.SECKILL_DIRECT_QUEUE, msg);
	}
	
	/*
	public void send(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send msg: "+msg);
		amqpTemplate.convertAndSend(MQConfig.DIRECT_QUEUE, msg);
	}
	
	public void sendTopic(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send topic msg: "+msg);
		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg+"1");
		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.#", msg+"2");
	}
	
	public void sendFanout(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send Fanout msg: "+msg);
		amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg);
	}
	
	public void sendHeaders(Object message) {
		String msg = RedisService.beanToString(message);
		log.info("send Headers msg: "+msg);
		MessageProperties properties = new MessageProperties();
		properties.setHeader("headers1", "value1");
		properties.setHeader("headers2", "value2");
		Message obj = new Message(msg.getBytes(), properties);
		amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
	}
	 */

}
