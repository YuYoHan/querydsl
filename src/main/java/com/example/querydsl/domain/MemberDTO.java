package com.example.querydsl.domain;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@ToString
@Getter
@NoArgsConstructor
public class MemberDTO {
    private String userName;
    private int age;

    @Builder
    @QueryProjection
    public MemberDTO(String userName, int age) {
        this.userName = userName;
        this.age = age;
    }
}