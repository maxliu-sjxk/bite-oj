/*
    表名： 小写；多个单词使用下划线连接；tb开头
*/


# 为什么不使用auto_increment？……，雪花算法


-- oj-system
create table tb_sys_user(
    user_id bigint unsigned not null comment '用户id(主键)',
    user_account varchar(20) not null comment '账号',
    password varchar(20) not null comment '密码',
    create_by bigint unsigned not null comment '创建者',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新者',
    update_time datetime comment '更新时间',

    primary key (user_id),
    unique key uk_user_account(user_account)
);

-- 加密算法引入后，密码字段改为char(60)
alter table tb_sys_user modify column password char(60);

-- 实现后端获取用户信息（昵称）时，添加nick_name(60)字段
alter table tb_sys_user add nick_name varchar(20) NULL AFTER user_account;