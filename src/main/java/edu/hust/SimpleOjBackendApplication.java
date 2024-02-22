package edu.hust;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("edu.hust.mapper")
public class SimpleOjBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleOjBackendApplication.class, args);
    }
    
}
