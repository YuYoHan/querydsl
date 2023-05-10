package com.example.devproject.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    public void testNoArgsConstructor() {
        Board board = new Board();

        System.out.println(board);
    }

    @Test
    public void  testRequiredArgsConstructor() {
        Board board = new Board("테스트 제목");
        System.out.println(board);
    }

}