package org.example.sanjiang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.sanjiang.mapper")
public class SanjiangApplication {
    public static void main(String[] args) {
        SpringApplication.run(SanjiangApplication.class, args);
    }
}
