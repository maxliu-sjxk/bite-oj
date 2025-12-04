package com.bite.api.domain.vo;

import com.bite.api.domain.UserExeResult;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.JudgeConstants;
import com.bite.common.core.enums.CodeRunStatus;
import com.bite.common.core.enums.QuestionResStatus;
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
    private Integer pass; //0-未通过 1-通过 2-未提交 3-判题中

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
        return UserQuestionResultVO.builder().pass(QuestionResStatus.PASSED.getValue()).userExeResultList(userExeResultList)
                .exeMessage(JudgeConstants.SUCCESS_ANSWER).score(JudgeConstants.DEFAULT_SCORE * difficulty).build();
    }

    /**
     * 失败
     * @param exeMsg
     * @return
     */
    public static UserQuestionResultVO fail(String exeMsg) {
        return UserQuestionResultVO.builder().pass(QuestionResStatus.NOT_PASSED.getValue()).userExeResultList(null)
                .exeMessage(exeMsg).score(JudgeConstants.ERROR_SCORE).build();
    }

    public static UserQuestionResultVO fail(List<UserExeResult> userExeResultList, String exeMsg) {
        return UserQuestionResultVO.builder().pass(QuestionResStatus.NOT_PASSED.getValue()).userExeResultList(userExeResultList)
                .exeMessage(exeMsg).score(JudgeConstants.ERROR_SCORE).build();
    }

    /**
     * 未提交
     * @return
     */
    public static UserQuestionResultVO notSubmit() {
        return UserQuestionResultVO.builder().pass(QuestionResStatus.NOT_SUBMIT.getValue()).build();
    }

    /**
     * 判题中
     * @return
     */
    public static UserQuestionResultVO judging() {
        return UserQuestionResultVO.builder().pass(QuestionResStatus.JUDGING.getValue()).build();
    }

    public static UserQuestionResultVO custom(Integer pass, List<UserExeResult> userExeResultList, String exeMessage) {
        return UserQuestionResultVO.builder().pass(pass).userExeResultList(userExeResultList)
                .exeMessage(exeMessage).build();
    }
}
