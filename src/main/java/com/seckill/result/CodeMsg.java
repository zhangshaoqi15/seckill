package com.seckill.result;

public class CodeMsg {
	private int code;	//状态码
	private String msg;	//消息
	public static CodeMsg SUCCESS = new CodeMsg(2000, "success");
	public static CodeMsg SERVER_ERROR = new CodeMsg(5000, "Server error!");
	public static CodeMsg BIND_ERROR = new CodeMsg(5001, "校验异常：%s");
	public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(5002, "request illegal");
	public static CodeMsg ACCESS_LIMITED = new CodeMsg(5002, "access too much!");
	
	public static CodeMsg SESSION_ERROR = new CodeMsg(4000, "session empty!");
	public static CodeMsg PASSWORD_EMPTY = new CodeMsg(4001, "password empty!");
	public static CodeMsg MOBILE_EMPTY = new CodeMsg(4002, "mobile empty!");
	public static CodeMsg MOBILE_ERROR = new CodeMsg(4003, "mobile error!");
	public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(4004, "mobile not exist!");
	public static CodeMsg PASSWORD_ERROR = new CodeMsg(4005, "password error!");
	public static CodeMsg VERIFYCODE_ERROR = new CodeMsg(4006, "验证码错误!");
	
	public static CodeMsg SECKILL_OVER = new CodeMsg(3030, "商品已经售罄！");
	public static CodeMsg REPEATE_SECKILL = new CodeMsg(3031, "不能重复秒杀！");
	public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(3032, "订单不存在！");
	public static CodeMsg SECKILL_FAILED = new CodeMsg(3033, "秒杀失败！");
	
	//constructor
	private CodeMsg(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	//Object类型的可变参数，format()把带转换符的msg转换参数后，返回CodeMsg
	public CodeMsg fillArgs(Object... args) {
		int code = this.code;
		String msg = String.format(this.msg, args);
		return new CodeMsg(code, msg);
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
