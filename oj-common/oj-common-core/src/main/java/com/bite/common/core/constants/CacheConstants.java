package com.bite.common.core.constants;

public class CacheConstants {

    //token键前缀
    public static final String LOGIN_TOKEN_KEY_PREFIX = "jwt:token:";

    //token有效期，单位：分钟
    public static final long EXP = 720;

    public static final long USER_EXP = 10;

    //token有效期延长阈值，单位：分钟
    public static final long REFRESH_TIME = 180;

    //邮箱验证码键前缀
    public static final String EMAIL_CODE_KEY_PREFIX = "email:code:";

    //邮箱验证码有效期，单位：分钟
    public static final long EMAIL_CODE_EXP = 5;

    //邮箱验证码发送次数键前缀
    public static final String CODE_TIMES_KEY_PREFIX = "code:times:";

    //邮箱验证码允许发送最小间隔，单位：秒
    public static final long MIN_SEND_INTERVAL = 60;

    public static final String EXAM_UNFINISHED_LIST_KEY = "exam:unfinished:list";

    public static final String EXAM_HISTORY_LIST_KEY = "exam:history:list";

    public static final String EXAM_DETAIL_KEY_PREFIX = "exam:detail:";


    public static final String USER_EXAM_LIST_KEY_PREFIX = "user:exam:list:";

    public static final String USER_DETAIL_KEY_PREFIX = "user:detail:";

    public static final String USER_UPLOAD_TIMES_KEY = "user:upload:times";

    public static final String QUESTION_LIST_KEY = "question:list";

    public static final String EXAM_QUESTION_LIST_KEY_PREFIX = "exam:question:list:";
}
