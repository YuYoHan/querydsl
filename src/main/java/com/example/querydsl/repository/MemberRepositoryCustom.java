package com.example.querydsl.repository;

import com.example.querydsl.domain.MemberSearchCondition;
import com.example.querydsl.domain.MemberTeamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDTO> search(MemberSearchCondition memberSearchCondition);
    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition memberSearchCondition, Pageable pageable);
    Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition memberSearchCondition,
                                          Pageable pageable,
                                          String search);
}
