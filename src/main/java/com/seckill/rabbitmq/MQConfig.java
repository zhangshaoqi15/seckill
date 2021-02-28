package com.seckill.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MQConfig {
	public static final String SECKILL_DIRECT_QUEUE = "seckill.queue";
	/*
	public static final String DIRECT_QUEUE = "direct.queue";
	public static final String TOPIC_QUEUE1 = "topic.queue1";
	public static final String TOPIC_QUEUE2 = "topic.queue2";
	public static final String HEADERS_QUEUE = "headers.queue";
	
	public static final String TOPIC_EXCHANGE = "topicExchange";
	public static final String FANOUT_EXCHANGE = "fanoutExchange";
	public static final String HEADERS_EXCHANGE = "headersExchange";
	*/
	
	/**
	 * Direct exchange模式
	 */
	@Bean
	public Queue directQueue() {
		return new Queue(SECKILL_DIRECT_QUEUE, true);
	}
	
	/**
	 * Topic exchange模式
	 */
//	@Bean
//	public Queue topicQueue1() {
//		return new Queue(TOPIC_QUEUE1, true);
//	}
//	@Bean
//	public Queue topicQueue2() {
//		return new Queue(TOPIC_QUEUE2, true);
//	}
//	@Bean
//	public TopicExchange topicExchange() {
//		return new TopicExchange(TOPIC_EXCHANGE);
//	}
//	@Bean
//	public Binding topicbinding1() {
//		return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
//	}
//	@Bean
//	public Binding topicbinding2() {
//		return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.key2");
//	}
	
	/**
	 * Fanout exchange模式
	 */
//	@Bean
//	public FanoutExchange fanoutExchange() {
//		return new FanoutExchange(FANOUT_EXCHANGE);
//	}
//	@Bean
//	public Binding fanoutbinding1() {
//		return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
//	}
//	@Bean
//	public Binding fanoutbinding2() {
//		return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
//	}
	
	/**
	 * Headers exchange模式
	 */
//	@Bean
//	public HeadersExchange headersExchange() {
//		return new HeadersExchange(HEADERS_EXCHANGE);
//	}
//	@Bean
//	public Queue headersQueue() {
//		return new Queue(HEADERS_QUEUE, true);
//	}
//	@Bean
//	public Binding headersbinding() {
//		Map<String, Object>map = new HashMap<String, Object>();
//		map.put("headers1", "value1");
//		map.put("headers2", "value2");
//		return BindingBuilder.bind(headersQueue()).to(headersExchange()).
//				whereAll(map).match();
//	}
	
}
