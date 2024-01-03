package com.example.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@SpringBootApplication
public class QuerydslApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuerydslApplication.class, args);
    }

    @Bean
    JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return  new JPAQueryFactory(em);
    }

//    @Bean
//    public PageableHandlerMethodArgumentResolverCustomizer customize() {
//        return p -> {
//            p.setOneIndexedParameters(true);    // 1 페이지 부터 시작
//            p.setMaxPageSize(10);               // 한 페이지에 10개씩 출력
//        };
//    }
}
