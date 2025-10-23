package com.bite.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultCode {

    SUCCESS                     (1000, "操作成功"),

    ERROR                       (2000, "服务器繁忙请稍后重试"),

    FAILED                      (3000, "操作失败"),
    FAILED_UNAUTHORIZED         (3001, "未授权"),
    FAILED_PARAMS_VALIDATE      (3002, "参数校验失败"),
    FAILED_NOT_EXISTS           (3003, "资源不存在"),
    FAILED_ALREADY_EXISTS       (3004, "资源已存在"),

    AILED_USER_EXISTS           (3101, "用户已存在"),
    FAILED_USER_NOT_EXISTS      (3102, "用户不存在"),
    FAILED_LOGIN                (3103, "账号或密码错误"),
    FAILED_USER_BANNED          (3104, "您已被列入黑名单，请联系管理员"),
    FAILED_EMAIL_SEND           (3105, "邮件发送失败"),
    FAILED_FREQUENT_REQUEST     (3106, "请求过于频繁，请稍后再试"),
    FAILED_TIMES_LIMIT          (3107, "当天请求次数已达上限"),
    FAILED_INVALID_CODE         (3108, "验证码无效"),
    FAILED_ERROR_CODE           (3108, "验证码错误"),


    EXAM_START_TIME_TOO_EARLY   (3201, "竞赛开始时间不能早于当前时间"),
    EXAM_START_TIME_TOO_LATE    (3202, "竞赛开始时间不能晚于结束时间"),
    EXAM_QUESTION_NOT_EXISTS    (3203, "添加了不存在的题目"),
    EXAM_ALREADY_STARTED        (3204, "竞赛已开始，不能执行任何操作"),
    EXAM_NOT_HAS_QUESTION       (3205, "竞赛需至少包含一个题目"),
    EXAM_ALREADY_ENDED          (3206, "竞赛已完赛，不能执行任何操作"),

    FAILED_REPEATED_ENTER       (3301, "已报名，无需重复报名");



    private int code;
    private String msg;

}


