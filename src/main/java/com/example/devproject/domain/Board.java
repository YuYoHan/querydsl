package com.example.devproject.domain;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "boardNo")
@ToString
public class Board {
    private int boardNo;
    @NonNull
    private String title;
    private String content;
    private String writer;
    private LocalDateTime regDate;
}
