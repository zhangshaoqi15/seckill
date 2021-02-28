package com.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.seckill.*")
@MapperScan(basePackages = "com.seckill.dao")
public class mainApplication {

	public static void main(String[] args) {
		SpringApplication.run(mainApplication.class, args);
	}

}