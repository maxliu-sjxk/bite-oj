package com.bite.common.core.constants;

public class CacheConstants {

    //token键前缀
    public static final String LOGIN_TOKEN_KEY_PREFIX = "jwt:token:";

    //token有效期，单位：分钟
    public static final long EXP = 720;

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
}
