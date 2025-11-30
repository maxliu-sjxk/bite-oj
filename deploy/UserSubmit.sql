

-- 用户提交表
create table tb_user_submit (
    submit_id bigint unsigned NOT NULL COMMENT '提交记录id',
    user_id bigint unsigned NOT NULL COMMENT '用户id',
    question_id bigint unsigned NOT NULL COMMENT '题目id',
    exam_id bigint unsigned COMMENT '竞赛id',

    program_type tinyint NOT NULL COMMENT '代码类型 0 java 1 CPP',
    user_code text NOT NULL COMMENT '用户代码',
    pass tinyint NOT NULL COMMENT '0：未通过  1：通过',
    exe_message varchar(500) NOT NULL COMMENT '执行结果',
    score int NOT NULL DEFAULT '0' COMMENT '得分',
    create_by bigint unsigned not null comment '创建人',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新人',
    update_time datetime comment '更新时间',

    primary key(`submit_id`)
)