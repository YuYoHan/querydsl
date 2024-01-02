package com.example.querydsl.domain;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class MemberTeamDTO {
    private Long memberId;
    private String userName;
    private int age;
    private Long teamId;
    private String teamName;

    @Builder
    @QueryProjection
    public MemberTeamDTO(Long memberId, String userName, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.userName = userName;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
