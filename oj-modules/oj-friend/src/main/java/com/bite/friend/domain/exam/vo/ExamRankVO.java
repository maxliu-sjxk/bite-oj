package com.bite.friend.domain.exam.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamRankVO {

    @JsonIgnore
    private Long userId;

    private String nickName;

    private int score;

    private int examRank;
}
