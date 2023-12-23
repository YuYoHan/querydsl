package com.example.querydsl.entity;

import com.querydsl.core.QueryResults;
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

    @Test
    void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.userName.eq("member1")
                        .and(member.age.between(10, 20)))
                .fetchOne();
        assertThat(findMember.getUserName()).isEqualTo("member1");
    }

    @Test
    void search2() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.userName.eq("member1")
                        , member.age.eq(10))
                .fetch();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void resultFetch() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();
        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        results.getTotal();

        queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /*
    *   회원 정렬 순서
    *   1. 회원 나이 내림차순(desc)
    *   2. 회원 이름 올림차순(asc)
    *   단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
    * */
    @Test
    void sort() {
        em.persist(Member.builder().userName(null).age(18));
        em.persist(Member.builder().userName("member5").age(18));
        em.persist(Member.builder().userName("member6").age(18));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(18))
                .orderBy(member.age.desc(), member.userName.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUserName()).isEqualTo("member5");
        assertThat(member6.getUserName()).isEqualTo("member6");
        assertThat(memberNull.getUserName()).isNull();
    }

    // 페이징
    @Test
    void paging1() {
        queryFactory
                .selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(1)
                .limit(2)
                .fetch();
    }

    @Test
    void paging2() {
        queryFactory
                .selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
    }
}