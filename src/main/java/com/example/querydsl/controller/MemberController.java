package com.example.querydsl.controller;

import com.example.querydsl.domain.MemberSearchCondition;
import com.example.querydsl.domain.MemberTeamDTO;
import com.example.querydsl.repository.MemberJpaRepository;
import com.example.querydsl.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public ResponseEntity<?> searchMemberV2(MemberSearchCondition condition,
                                            @PageableDefault(
                                                    sort = "id", direction = Sort.Direction.ASC)
                                            Pageable pageable,
                                            String search) {
        Page<MemberTeamDTO> memberTeamDTOS = memberRepository.searchPageComplex(condition, pageable, search);
        Map<String, Object> response = new HashMap<>();
        // 현재 페이지의 아이템 목록
        response.put("members", memberTeamDTOS.getContent());
        // 현재 페이지 번호
        response.put("nowPageNumber", memberTeamDTOS.getNumber()+1);
        // 전체 페이지 수
        response.put("totalPage", memberTeamDTOS.getTotalPages());
        // 한 페이지에 출력되는 데이터 개수
        response.put("pageSize", memberTeamDTOS.getSize());
        // 다음 페이지 존재 여부
        response.put("hasNextPage", memberTeamDTOS.hasNext());
        // 이전 페이지 존재 여부
        response.put("hasPreviousPage", memberTeamDTOS.hasPrevious());
        // 첫 번째 페이지 여부
        response.put("isFirstPage", memberTeamDTOS.isFirst());
        // 마지막 페이지 여부
        response.put("isLastPage", memberTeamDTOS.isLast());
        return ResponseEntity.ok().body(response);
    }
}
