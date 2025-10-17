package com.bite.friend.domain.exam.dto;

import com.bite.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamQueryDTO extends PageQueryDTO {

    //拓展：根据竞赛名称搜索
    private String title;

    private String startTime;

    private String endTime;

    //是否完赛: 0: 未完赛 1: 已完赛
    private Integer type;

}
