package com.example.querydsl.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@NoArgsConstructor
public class MemberSearchCondition {
    // 회원명, 팀명, 나이(ageGoe, ageLoe)

    private String userName;
    private String teamName;
    // 나이가 크거나 같거나
    private Integer ageGoe;
    // 나이가 작거나 같거나
    private Integer ageLoe;

    @Builder
    public MemberSearchCondition(String userName, String teamName, Integer ageGoe, Integer ageLoe) {
        this.userName = userName;
        this.teamName = teamName;
        this.ageGoe = ageGoe;
        this.ageLoe = ageLoe;
    }
}
