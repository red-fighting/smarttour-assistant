package com.panduoma.trevaljava;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 扫描mapper包路径
@MapperScan("com.panduoma.trevaljava.mapper")
@SpringBootApplication
public class TrevalJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrevalJavaApplication.class, args);
	}

}
