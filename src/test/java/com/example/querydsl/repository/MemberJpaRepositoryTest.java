package com.example.querydsl.repository;

import com.example.querydsl.entity.Member;
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

}