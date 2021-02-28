package com.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.seckill.result.CodeMsg;
import com.seckill.result.Result;
import com.seckill.rabbitmq.Sender;
import com.seckill.redis.RedisService;

@Controller
public class MyController {
	
	@Autowired
	RedisService redisService;
	@Autowired
	Sender sender;

	@RequestMapping("/")
	@ResponseBody
	public String hello() {
		return String.format("Hello111 seckill!!!");
	}
	
	@RequestMapping("/secError")
	@ResponseBody
	public Result<String> secError() {
		return Result.secError(CodeMsg.SERVER_ERROR);
	}
	
	/*
	@RequestMapping("/directSend")
	@ResponseBody
	public Result<String> directSend() {
		sender.send("message from directSender");
		return Result.success("sended");
	}
	
	@RequestMapping("/topicSend")
	@ResponseBody
	public Result<String> topicSend() {
		sender.sendTopic("message from topicSender");
		return Result.success("sended");
	}
	
	@RequestMapping("/fanoutSend")
	@ResponseBody
	public Result<String> fanoutSend() {
		sender.sendFanout("message from fanoutSender");
		return Result.success("sended");
	}
	
	@RequestMapping("/headersSend")
	@ResponseBody
	public Result<String> headersSend() {
		sender.sendHeaders("message from headersSender");
		return Result.success("sended");
	}
	*/
}