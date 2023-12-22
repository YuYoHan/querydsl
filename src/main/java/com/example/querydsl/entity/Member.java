package com.example.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "query_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@ToString(of = {"id", "userName", "age"})
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String userName;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;


    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
