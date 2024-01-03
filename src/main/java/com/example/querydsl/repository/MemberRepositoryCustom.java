package com.example.querydsl.repository;

import com.example.querydsl.domain.MemberSearchCondition;
import com.example.querydsl.domain.MemberTeamDTO;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDTO> search(MemberSearchCondition memberSearchCondition);
}
