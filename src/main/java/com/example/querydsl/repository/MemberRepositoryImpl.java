package com.example.querydsl.repository;

import com.example.querydsl.domain.MemberSearchCondition;
import com.example.querydsl.domain.MemberTeamDTO;
import com.example.querydsl.domain.QMemberTeamDTO;
import com.example.querydsl.entity.Member;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.domain.Sort.Order;

import java.util.List;


import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;
import static com.querydsl.core.types.Order.*;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<MemberTeamDTO> search(MemberSearchCondition condition) {

        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.userName,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition,
                                                Pageable pageable) {
        QueryResults<MemberTeamDTO> result = queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.userName,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDTO> content = result.getResults();
        long total = result.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition,
                                                 Pageable pageable,
                                                 String search) {
        List<MemberTeamDTO> content = queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.userName,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()),
                        searchKeyword(search))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(sort(pageable))
                .fetch();


        // count 쿼리 (조건에 부합하는 로우의 총 개수를 얻는 것이기 때문에 페이징 미적용)
        JPAQuery<Long> countQuery = queryFactory
                // SQL 상으로는 count(member.id)와 동일
                .select(member.count())
                .from(member)
                .leftJoin(member.team, team)
                .where(userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()),
                        searchKeyword(search));

        // 페이지 시작이거나 컨텐츠의 사이즈가 페이지 사이즈보다 작거나
        // 마지막 페이지 일 대 카운트 쿼리를 호출하지 않는다.
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
       // return new PageImpl<>(content, pageable, total);
    }

    // OrderSpecifier<?>는 QueryDSL에서 사용되는 정렬을 나타내는 클래스
    // OrderSpecifier는 정렬의 종류와 방향을 나타내는 객체로, QueryDSL에서 쿼리를 작성할 때 사용됩니다.
    // 즉, OrderSpecifier<?>를 사용하여 동적인 정렬을 표현할 수 있습니다.
    private OrderSpecifier<?> sort(Pageable pageable) {
        if (!pageable.getSort().isEmpty()) {
            for (Order order : pageable.getSort()) {
                // 정렬이 오름차순인지 여부를 확인하는 코드입니다.
                // 맞다면 오름차순, 아니면 내림차순으로 진행합니다.
                com.querydsl.core.types.Order direction = order.getDirection().isAscending()
                        ? ASC : DESC;

                switch (order.getProperty()) {
                    case "memberId":
                        return new OrderSpecifier<>(direction, member.id);
                    case "userName":
                        return new OrderSpecifier<>(direction, member.userName);
                }
            }
        }
        return member.id.desc();
    }


    @Override
    public Page<Member> search2(MemberSearchCondition condition,
                                Pageable pageable) {
        JPAQuery<Member> query = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        JPAQuery<Long> count = queryFactory
                .select(member.count())
                .from(member)
                .where(userNameEq(condition.getUserName()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()));

        for (Order order : pageable.getSort()) {
            // PathBuilder는 주어진 엔터티의 동적인 경로를 생성하는 데 사용됩니다.
            PathBuilder pathBuilder = new PathBuilder(
                    // 엔티티의 타입 정보를 얻어온다.
                    member.getType(),
                    // 엔티티의 메타데이터를 얻어온다.
                    member.getMetadata());
            // Order 객체에서 정의된 속성에 해당하는 동적 경로를 얻어오게 됩니다.
            // 예를 들어, 만약 order.getProperty()가 "userName"이라면,
            // pathBuilder.get("userName")는 엔터티의 "userName" 속성에 대한 동적 경로를 반환하게 됩니다.
            // 이 동적 경로는 QueryDSL에서 사용되어 정렬 조건을 만들 때 활용됩니다.
            PathBuilder sort = pathBuilder.get(order.getProperty());

            query.orderBy(
                    new OrderSpecifier<>(
                            order.isAscending() ? ASC : DESC,
                            sort != null ? sort : member.id
                    ));
        }
        List<Member> result = query.fetch();
        return PageableExecutionUtils.getPage(result, pageable, count::fetchOne);
    }

    private BooleanExpression userNameEq(String userName) {
        return hasText(userName) ? member.userName.eq(userName) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression searchKeyword(String search) {
        // likeIgnoreCase는 QueryDSL에서 문자열에 대한 대소문자를 무시하고 부분 일치 검색을 수행하는 메서드입니다.
        // 이 메서드는 SQL에서의 LIKE 연산과 유사하지만, 대소문자를 구분하지 않고 비교합니다.
        return hasText(search) ? member.userName.likeIgnoreCase("%" + search + "%") : null;
    }
}
