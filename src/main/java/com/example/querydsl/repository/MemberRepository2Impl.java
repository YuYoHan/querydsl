//package com.example.querydsl.repository;
//
//import com.example.querydsl.domain.MemberSearchCondition;
//import com.example.querydsl.domain.MemberTeamDTO;
//import com.example.querydsl.domain.QMemberTeamDTO;
//import com.example.querydsl.entity.Member;
//import com.querydsl.core.QueryResults;
//import com.querydsl.core.types.dsl.BooleanExpression;
//import com.querydsl.jpa.JPQLQuery;
//import com.querydsl.jpa.impl.JPAQuery;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import jakarta.persistence.EntityManager;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
//import org.springframework.data.support.PageableExecutionUtils;
//
//import java.util.List;
//
//import static com.example.querydsl.entity.QMember.member;
//import static com.example.querydsl.entity.QTeam.team;
//import static org.springframework.util.StringUtils.hasText;
//
//
//public class MemberRepository2Impl extends QuerydslRepositorySupport implements MemberRepositoryCustom{
//
//    private final JPAQueryFactory queryFactory;
//
//
//    public MemberRepository2Impl(EntityManager em) {
//        super(Member.class);
//        this.queryFactory = new JPAQueryFactory(em);
//    }
//
//    @Override
//    public List<MemberTeamDTO> search(MemberSearchCondition condition) {
//
//        return from(member)
//                .leftJoin(member.team, team)
//                .where(userNameEq(condition.getUserName()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe()))
//                .select(new QMemberTeamDTO(
//                        member.id.as("memberId"),
//                        member.userName,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")))
//                .fetch();
//    }
//
//    @Override
//    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition,
//                                                Pageable pageable) {
//
//        JPQLQuery<MemberTeamDTO> jpaQuery = from(member)
//                .leftJoin(member.team, team)
//                .where(userNameEq(condition.getUserName()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe()))
//                .select(new QMemberTeamDTO(
//                        member.id.as("memberId"),
//                        member.userName,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")));
//
//        JPQLQuery<MemberTeamDTO> query = getQuerydsl().applyPagination(pageable, jpaQuery);
//
//        List<MemberTeamDTO> fetch = query.fetch();
//
//        List<MemberTeamDTO> content = result.getResults();
//        long total = result.getTotal();
//        return new PageImpl<>(content, pageable, total);
//    }
//
//    @Override
//    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition,
//                                                 Pageable pageable,
//                                                 String search) {
//        List<MemberTeamDTO> content = queryFactory
//                .select(new QMemberTeamDTO(
//                        member.id.as("memberId"),
//                        member.userName,
//                        member.age,
//                        team.id.as("teamId"),
//                        team.name.as("teamName")))
//                .from(member)
//                .leftJoin(member.team, team)
//                .where(userNameEq(condition.getUserName()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe()),
//                        searchKeyword(search))
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        // count 쿼리 (조건에 부합하는 로우의 총 개수를 얻는 것이기 때문에 페이징 미적용)
//        JPAQuery<Long> countQuery = queryFactory
//                // SQL 상으로는 count(member.id)와 동일
//                .select(member.count())
//                .from(member)
//                .leftJoin(member.team, team)
//                .where(userNameEq(condition.getUserName()),
//                        teamNameEq(condition.getTeamName()),
//                        ageGoe(condition.getAgeGoe()),
//                        ageLoe(condition.getAgeLoe()),
//                        searchKeyword(search));
//
//        // 페이지 시작이거나 컨텐츠의 사이즈가 페이지 사이즈보다 작거나
//        // 마지막 페이지 일 대 카운트 쿼리를 호출하지 않는다.
//        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
////        return new PageImpl<>(content, pageable, total);
//    }
//
//    private BooleanExpression userNameEq(String userName) {
//        return hasText(userName) ? member.userName.eq(userName) : null;
//    }
//
//    private BooleanExpression teamNameEq(String teamName) {
//        return hasText(teamName) ? team.name.eq(teamName) : null;
//    }
//
//    private BooleanExpression ageGoe(Integer ageGoe) {
//        return ageGoe != null ? member.age.goe(ageGoe) : null;
//    }
//
//    private BooleanExpression ageLoe(Integer ageLoe) {
//        return ageLoe != null ? member.age.loe(ageLoe) : null;
//    }
//
//    private BooleanExpression searchKeyword(String search) {
//        // likeIgnoreCase는 QueryDSL에서 문자열에 대한 대소문자를 무시하고 부분 일치 검색을 수행하는 메서드입니다.
//        // 이 메서드는 SQL에서의 LIKE 연산과 유사하지만, 대소문자를 구분하지 않고 비교합니다.
//        return hasText(search) ? member.userName.likeIgnoreCase("%" + search + "%") : null;
//    }
//}
