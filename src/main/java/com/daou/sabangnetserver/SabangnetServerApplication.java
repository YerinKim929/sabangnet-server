package com.daou.sabangnetserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class SabangnetServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SabangnetServerApplication.class, args);
    }

}

