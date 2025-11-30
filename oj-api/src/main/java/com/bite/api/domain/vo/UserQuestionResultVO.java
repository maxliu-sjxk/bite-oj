package com.bite.api.domain.vo;

import com.bite.api.domain.UserExeResult;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.JudgeConstants;
import com.bite.common.core.enums.CodeRunStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class UserQuestionResultVO {

    /**
     * 是否通过标识
     */
    private Integer pass; //0-未通过 1-通过

    /**
     * 运行结果
     * - 输入
     * - 输出
     * - 预期输出
     */
    private List<UserExeResult> userExeResultList;

    /**
     * 执行信息
     */
    private String exeMessage;

    /**
     * 分数
     */
    @JsonIgnore
    private Integer score;

    /**
     * 建造者模式 + 静态工厂
     * 成功
     * 固定属性：pass、exeMsg
     * @param userExeResultList 运行结果列表
     * @param difficulty 题目难度，用于计算分数
     * @return
     */
    public static UserQuestionResultVO success(List<UserExeResult> userExeResultList, Integer difficulty) {
        return UserQuestionResultVO.builder().pass(Constants.TRUE).userExeResultList(userExeResultList)
                .exeMessage(JudgeConstants.SUCCESS_ANSWER).score(JudgeConstants.DEFAULT_SCORE * difficulty).build();
    }

    /**
     * 失败
     * @param exeMsg
     * @return
     */
    public static UserQuestionResultVO fail(String exeMsg) {
        return UserQuestionResultVO.builder().pass(Constants.FALSE).userExeResultList(null)
                .exeMessage(exeMsg).score(JudgeConstants.ERROR_SCORE).build();
    }
}
