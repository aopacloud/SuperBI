package net.aopacloud.superbi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/28 14:04
 */
@SpringBootApplication
@MapperScan(basePackages = {"net.aopacloud.superbi.mapper"})
public class SuperBiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuperBiApplication.class, args);
    }
}
