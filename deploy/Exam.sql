-- 竞赛管理

-- A端：列表、新增、编辑、删除、发布、撤销发布
-- C端：列表（未结赛/历史）、报名参赛、我的比赛、参加竞赛（竞赛倒计时、竞赛内题目切换、完成竞赛）、竞赛练习、查看排名、消息通知

create table tb_exam (
    exam_id bigint unsigned not null comment '竞赛id(主键)',
    title varchar(50) not null comment '竞赛标题',
    start_time datetime not null comment '开始时间',
    end_time datetime not null comment '结束时间',
    status tinyint not null default '0' comment '是否发布：0：未发布 1：已发布',
    create_by bigint unsigned not null comment '创建者',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新者',
    update_time datetime comment '更新时间',
    primary key (exam_id)
);

-- 题目-竞赛关系表
create table tb_exam_question(
    exam_question_id bigint unsigned not null comment '题目-竞赛关系id(主键)',
    exam_id bigint unsigned not null comment '竞赛id',
    question_id bigint unsigned not null comment '题目id',
    question_order int not null comment '题目顺序',
    create_by bigint unsigned not null comment '创建者',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新者',
    update_time datetime comment '更新时间',
    primary key (exam_question_id)
);