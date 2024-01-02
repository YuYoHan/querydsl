package com.example.querydsl.repository;

import com.example.querydsl.domain.MemberSearchCondition;
import com.example.querydsl.domain.MemberTeamDTO;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {
        Member member = Member.builder()
                .userName("member1")
                .age(10)
                .build();

        memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.findById(member.getId())
                .orElseThrow(EntityNotFoundException::new);

        assertThat(findMember).isEqualTo(member);
        List<Member> all = memberJpaRepository.findAll();
        assertThat(all).containsExactly(member);

        List<Member> findByUserName = memberJpaRepository.findByUserName("member1");
        assertThat(findByUserName).containsExactly(member);
    }

    @Test
    void basicQuerydslTest() {
        Member member = Member.builder()
                .userName("member1")
                .age(10)
                .build();

        memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.findById(member.getId())
                .orElseThrow(EntityNotFoundException::new);

        assertThat(findMember).isEqualTo(member);
        List<Member> all = memberJpaRepository.findAll_Querydsl();
        assertThat(all).containsExactly(member);

        List<Member> findByUserName = memberJpaRepository.findByUserName_Querydsl("member1");
        assertThat(findByUserName).containsExactly(member);
    }

    @Test
    void searchTest() {
        Team teamA = Team.builder()
                .name("teamA")
                .build();
        Team teamB = Team.builder()
                .name("teamB")
                .build();
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder()
                .userName("member1")
                .age(10)
                .team(teamA)
                .build();
        Member member2 = Member.builder()
                .userName("member2")
                .age(20)
                .team(teamA)
                .build();
        Member member3 = Member.builder()
                .userName("member3")
                .age(30)
                .team(teamB)
                .build();
        Member member4 = Member.builder()
                .userName("member4")
                .age(40)
                .team(teamB)
                .build();
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = MemberSearchCondition.builder()
                .ageGoe(35)
                .ageLoe(40)
                .teamName("teamB")
                .build();

        List<MemberTeamDTO> result = memberJpaRepository.searchByBuilder(condition);
        assertThat(result).extracting("userName").containsExactly("member4");
    }

}