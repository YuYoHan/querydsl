package com.example.querydsl.service;

import com.example.querydsl.domain.MemberSearchCondition;
import com.example.querydsl.domain.MemberTeamDTO;
import com.example.querydsl.entity.Member;
import com.example.querydsl.repository.MemberTestRepository;
import com.querydsl.core.types.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberTestRepository memberTestRepository;

    public Page<MemberTeamDTO> search(MemberSearchCondition condition, Pageable pageable) {

        Sort sort = pageable.getSort();
        PageRequest pageRequest = PageRequest.of(
                (int) pageable.getOffset(),
                pageable.getPageSize(),
                sort
        );

        Page<Member> resultPage = memberTestRepository.applyPagination2(condition, pageRequest);
        return resultPage.map(member -> MemberTeamDTO.builder()
                .memberId(member.getId())
                .age(member.getAge())
                .userName(member.getUserName())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .build());
    }

    public Page<MemberTeamDTO> search2(MemberSearchCondition condition, Pageable pageable) {
        Sort sort = pageable.getSort();
        PageRequest pageRequest = PageRequest.of(
                (int) pageable.getOffset(),
                pageable.getPageSize(),
                sort
        );
        Page<Member> members = memberTestRepository.searchPageByApplyPage(condition, pageable);
        return members.map(member -> MemberTeamDTO.builder()
                .memberId(member.getId())
                .age(member.getAge())
                .userName(member.getUserName())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .build());
    }
}
