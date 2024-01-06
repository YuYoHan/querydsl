package com.example.querydsl.service;

import com.example.querydsl.domain.MemberSearchCondition;
import com.example.querydsl.domain.MemberTeamDTO;
import com.example.querydsl.entity.Member;
import com.example.querydsl.repository.MemberRepository;
import com.example.querydsl.repository.MemberTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberTestRepository memberTestRepository;
    private final MemberRepository memberRepository;

    public Page<MemberTeamDTO> search(MemberSearchCondition condition, Pageable pageable) {

        Page<Member> resultPage = memberTestRepository.applyPagination2(condition, pageable);
        return resultPage.map(member -> MemberTeamDTO.builder()
                .memberId(member.getId())
                .age(member.getAge())
                .userName(member.getUserName())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .build());
    }

    public Page<MemberTeamDTO> search2(MemberSearchCondition condition, Pageable pageable) {
        Page<Member> members = memberTestRepository.searchPageByApplyPage(condition, pageable);
        return members.map(member -> MemberTeamDTO.builder()
                .memberId(member.getId())
                .age(member.getAge())
                .userName(member.getUserName())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .build());
    }

    public Page<MemberTeamDTO> search3(MemberSearchCondition condition, Pageable pageable) {
        Page<Member> members = memberRepository.search2(condition, pageable);
        return members.map(member -> MemberTeamDTO.builder()
                .memberId(member.getId())
                .age(member.getAge())
                .userName(member.getUserName())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .build());
    }
}
