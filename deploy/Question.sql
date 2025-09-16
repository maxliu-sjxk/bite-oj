-- 题库管理

-- B端：题目列表、添加题目、编辑、删除
-- C端：题目列表、题目热榜、答题、竞赛开始答题、竞赛练习

create table tb_question(
    question_id bigint unsigned not null comment '题目id(主键)',
    title varchar(50) not null comment '题目标题',
    content varchar(1000) not null comment '题目内容',
    difficulty tinyint not null comment '题目难度 1：简单 2：中等 3：困难',
    time_limit int not null comment '题目时间限制',
    space_limit int not null comment '题目空间限制',
    question_case varchar(1000) comment '题目样例',
    default_code varchar(500) not null comment '题目默认代码',
    main_func varchar(500) not null comment 'main函数',
    create_by bigint unsigned not null comment '创建者',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新者',
    update_time datetime comment '更新时间',
    primary key (question_id)
);