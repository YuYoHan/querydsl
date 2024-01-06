package com.example.querydsl.domain;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.example.querydsl.domain.QMemberDTO is a Querydsl Projection type for MemberDTO
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberDTO extends ConstructorExpression<MemberDTO> {

    private static final long serialVersionUID = 1982662723L;

    public QMemberDTO(com.querydsl.core.types.Expression<String> userName, com.querydsl.core.types.Expression<Integer> age) {
        super(MemberDTO.class, new Class<?>[]{String.class, int.class}, userName, age);
    }

}

