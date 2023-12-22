package com.example.querydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Commit
@Log4j2
class MemberTest {
    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    @Test
    public void testEntity() {
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

        // 초기화
        em.flush();
        em.clear();

        // 확인
        List<Member> members = em.createQuery(
                "select m from query_members m",
                Member.class)
                .getResultList();

        for (Member member : members) {
            log.info("member : " + member);
            log.info("→ member.team : " + member.getTeam());
        }
    }

    @Test
    public void startJPQL() {
        String q1String = "select m from query_members m " +
                "where m.userName = :userName";

        Member findMember = em.createQuery(q1String, Member.class)
                .setParameter("userName", "member1")
                .getSingleResult();

        log.info("findMember : " + findMember);
        assertThat(findMember.getUserName()).isEqualTo("member1");
    }

    @Test
    @DisplayName(value = "querydsl 테스트")
    void startQueryDsl() {
        queryFactory = new JPAQueryFactory(em);
        QMember qMember = new QMember("m");

        Member findMember = queryFactory
                .select(qMember)
                .from(qMember)
                .where(qMember.userName.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUserName()).isEqualTo("member1");
    }

    @Test
    void testQueryDsl() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.userName.eq("member1"))
                .fetchOne();
        assertThat(findMember.getUserName()).isEqualTo("member1");
    }
}