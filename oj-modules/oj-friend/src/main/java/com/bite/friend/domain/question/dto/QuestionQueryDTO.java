package com.bite.friend.domain.question.dto;

import com.bite.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class QuestionQueryDTO extends PageQueryDTO {

   /**
    * 搜索关键字：题目 或 内容
    */
   private String keyword;

   private Integer difficulty;
}
