package com.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5util {

	private static final String salt = "123456";
	
	/*
	 * 调用codec的包进行MD5加密
	 */
	public static String MD5(String str) {
		return DigestUtils.md5Hex(str);
	}
	
	/*
	 * 第一次MD5，在请求中的密码
	 */
	public static String browserPass(String pass) {
		String str = salt.charAt(2) + pass + salt.charAt(4);
		return MD5(str);
	}
	
	/*
	 * 第二次MD5，存放在服务端的密码
	 */
	public static String DBPass(String pass, String RamSalt) {
		String str = RamSalt.charAt(3) + pass + RamSalt.charAt(5);
		return MD5(str);
	}
	
	/*
	public static void main(String[] args) {
		System.out.println(browserPass("123")); // d70f195edbc052d647a288e3d46b3b2e
		System.out.println(DBPass("d70f195edbc052d647a288e3d46b3b2e", "654321")); // 737a7e28ca63858e689a7357c8e7db5a
	}
	*/
}
