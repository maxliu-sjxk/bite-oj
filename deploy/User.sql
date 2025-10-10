-- 用户管理（C端）
-- B端：C端用户列表、拉黑
-- C端：登录、注册、退出登录、个人中心


create table tb_user(
    user_id bigint unsigned not null comment '用户id（主键）',
    nick_name varchar(20) comment '用户昵称',
    head_image varchar(100) comment '用户头像（具体存储路径）',
    sex tinyint comment '性别 1：男 2：女',
    phone char(11) not null comment '手机号',

    code char(6) comment '验证码，是否存在待定',
    email varchar(20) not null comment '邮箱，not null 因为计划邮箱登录',

    wechat varchar(20) comment '微信号',
    school_name varchar(20) comment '学校',
    major_name varchar(20) comment '专业',
    introduce varchar(100) comment '个人介绍',
    status tinyint not null comment '用户状态 0：拉黑 1：正常',
    create_by bigint unsigned not null comment '创建者',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新者',
    update_time datetime comment '更新时间',

    primary key (user_id)
);