package com.seckill.result;

public class Result<T> {
	private int code;	//状态码
	private String msg;	//消息
	private T data;		//数据
	
	//constructor：请求成功时创建
	private Result(T data) {
		this.code = 2000;
		this.msg = "succeeed";
		this.data = data;
	}
	
	//constructor：请求失败时创建
	private Result(CodeMsg cm) {
		if(cm == null)
			return;
		
		this.code = cm.getCode();
		this.msg = cm.getMsg();
	}
	
	//请求成功时调用
	public static <T> Result<T> success(T data) {
		return new Result<T>(data); 
	}
	
	//请求失败时调用
	public static <T> Result<T> secError(CodeMsg cm) {
		return new Result<T>(cm); 
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
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}
