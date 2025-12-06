package com.bite.job.domain.exam;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamRankInfo {

    private Long userId;

    private int score;

    private int examRank;
}
