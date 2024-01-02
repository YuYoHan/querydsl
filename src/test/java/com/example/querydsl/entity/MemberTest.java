package com.example.querydsl.entity;

import com.example.querydsl.domain.MemberDTO;
import com.example.querydsl.domain.QMemberDTO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
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
import static com.example.querydsl.entity.QTeam.team;
import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.as;
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

    // 집합
    @Test
    void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(54);
    }

    // 팀 이름과 각 팀의 평균 연령을 구해라
    @Test
    void group() throws Exception{
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
    }

    // 팀 A에 속한 모든 회원
    @Test
    void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        // extracting은 AssertJ 라이브러리에서 제공하는 메서드 중 하나로,
        // 객체나 컬렉션에서 원하는 값을 추출하여 검증할 때 사용
        assertThat(result).extracting("userName")
                // containsExactly는 AssertJ 라이브러리에서 제공하는 메서드 중 하나로,
                // 주어진 컬렉션이나 배열이 정확하게 특정 순서로 주어진 값들을 포함하는지를 검증하는 데 사용
                .containsExactly("member1", "member2");
    }


    @Test
    void theta_join() {
        em.persist(Member.builder().userName("teamA"));
        em.persist(Member.builder().userName("teamB"));

        // 모든 멤버, 팀 테이블을 조회하고
        // 조인한 테이블에서 멤버의 이름이랑 팀의 이름이 같은거를 찾아서
        // 결과를 가져온다.
        queryFactory
                .select(member)
                .from(member, team)
                .where(member.userName.eq(team.name))
                .fetch();
    }

    // 회원과 팀을 조인하면서 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회;
    // JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
    // SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
    @Test
     void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(team.name.eq("teamA"))
                .fetch();
    }
    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    void join_on_no_relation() {
        em.persist(Member.builder().userName("teamA"));
        em.persist(Member.builder().userName("teamB"));

        // 모든 멤버, 팀 테이블을 조회하고
        // 조인한 테이블에서 멤버의 이름이랑 팀의 이름이 같은거를 찾아서
        // 결과를 가져온다.
        queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.userName.eq(team.name))
                .where(member.userName.eq(team.name))
                .fetch();
    }


    @PersistenceUnit
    EntityManagerFactory emf;
    // 페치조인 미적용
    // 지연로딩으로 Member, Team SQL 쿼리 각각 실행
    @Test
    void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.userName.eq("member1"))
                .fetchOne();

        // findMember.getTeam()이 로딩이 된 엔티티인지 아닌지 알려주는 기능
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.userName.eq("member1"))
                .fetchOne();

        // findMember.getTeam()이 로딩이 된 엔티티인지 아닌지 알려주는 기능
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    // 나이가 가장 많은 회원 조회
    @Test
    void subQuery() {

        // 이걸 하는 이유는 서브쿼리가 member하고 중복되지 않게 하기위해서
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(20);
    }

    // 나이가 평균 이상인 회원
    @Test
    void subQueryGoe() {

        // 이걸 하는 이유는 서브쿼리가 member하고 중복되지 않게 하기위해서
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(20);
    }

    @Test
    void subQueryIn() {

        // 이걸 하는 이유는 서브쿼리가 member하고 중복되지 않게 하기위해서
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(20);
    }

    @Test
    void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");
        queryFactory
                .select(member.userName,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();
    }

    @Test
    void basicCase() {
        queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
    }

    @Test
    void complexCase() {
        queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0,20)).then("0~20살")
                        .when(member.age.between(21,30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
    }

    // 상수
    @Test
    void constant() {
        queryFactory
                .select(member.userName, Expressions.constant("A"))
                .from(member)
                .fetch();
    }

    // 문자 더하기
    @Test
    void concat() {
        // {userName}_{age}
        queryFactory
                .select(member.userName.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.userName.eq("member1"))
                .fetch();
    }

    @Test
    void simpleProjection() {
        List<String> result = queryFactory
                .select(member.userName)
                .from(member)
                .fetch();
    }

    @Test
    void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.userName, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String userName = tuple.get(member.userName);
            Integer age = tuple.get(member.age);
        }
    }

    // JPA에서 DTO 조회
    @Test
    void findDtoByJPQL() {
        em.createQuery(
                "select new com.example.querydsl.domain.MemberDTO(m.userName, m.age) " +
                        "from query_members m", MemberDTO.class)
                .getResultList();
    }

    @Test
    void findDtoBySetter() {
        queryFactory
                .select(Projections.bean(MemberDTO.class,
                        member.userName,
                        member.age))
                .from(member)
                .fetch();
    }

    @Test
    void findDtoByField() {
        queryFactory
                .select(Projections.fields(MemberDTO.class,
                        member.userName,
                        member.age))
                .from(member)
                .fetch();
    }

    @Test
    void findDtoByConstructor() {
        queryFactory
                .select(Projections.constructor(MemberDTO.class,
                        member.userName,
                        member.age))
                .from(member)
                .fetch();
    }

    @Test
    void findUserDto() {

        QMember qMember = new QMember("memberSub");

        queryFactory
                .select(Projections.constructor(MemberDTO.class,
                        member.userName.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(qMember.age.max())
                                .from(qMember), "age")))
                .from(member)
                .fetch();
    }

    @Test
    void findDtoByQueryProjection() {
        List<MemberDTO> result = queryFactory
                .select(new QMemberDTO(member.userName, member.age))
                .from(member)
                .fetch();
    }

    @Test
    void dynamicQuery_BooleanBuilder() {
        String userNameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(userNameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String userNameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
        if(userNameCond != null) {
            builder.and(member.userName.eq(userNameCond));
        }

        if(ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamicQuery_whereParam() throws Exception{
        String userNameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(userNameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String userNameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
//                .where(userNameEq(userNameCond), ageEq(ageCond))
                .where(allEq(userNameCond, ageCond))
                .fetch();
    }

    private BooleanExpression userNameEq(String userNameCond) {
        return userNameCond != null ?
                member.userName.eq(userNameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ?
                member.age.eq(ageCond) : null;
    }

    // 이것의 장점은 아래와 같이 합칠 수 있다는 것이다.
    private BooleanExpression allEq(String userNameCond, Integer ageCond) {
        return userNameEq(userNameCond).and(ageEq(ageCond));
    }

    @Test
    @Commit
    void bulkUpdate() {

        // 기존에 영속성 컨텍스트랑 DB랑 일치되어 있지만
        // 여기서 업데이트를 하면 바로 DB가 수정된다.
        // 그래서 영속성 컨텍스트랑 차이가 생긴다.
        // 조회를 할 때 변경을 가져오려고 하지만
        // DB에서 변경되어도 영속성 컨텍스트가 변경되지 않으면
        // DB 데이터를 버리고 영속성 컨텍스트 데이터를 가져온다.
        queryFactory
                .update(member)
                .set(member.userName, "비회원")
                .where(member.age.lt(28))
                .execute();

        // 영속성 컨텍스트 초기화
        em.flush();
        em.clear();

        // 영속성 컨텍스트를 초기화해서 비어있기 때문에
        // DB에서 조회하므로 제대로 나온다.
        queryFactory
                .selectFrom(member)
                .fetch();
    }

    // 더하기
    @Test
    void bulkAdd() {
        queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }

    // 곱하기
    @Test
    void bulkMultiplication() {
        queryFactory
                .update(member)
                .set(member.age, member.age.multiply(1))
                .execute();
    }

    @Test
    void bulkDelete() {
        queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }

    @Test
    void sqlDFunction() {
        queryFactory
                .select(
                        Expressions.stringTemplate(
                                "function('replace', {0}, {1}, {2})",
                                member.userName,
                                "member",
                                "M"
                        )).from(member)
                .fetch();
    }

    @Test
    void sqlFunction2() {
        queryFactory
                .select(member.userName)
                .from(member)
//                .where(member.userName.eq(
//                        Expressions.stringTemplate(
//                                "function('lower', {0})",
//                                member.userName
//                        )
//                ))
                .where(member.userName.eq(member.userName.lower()))
                .fetch();
    }
}