
- 竞赛报名
- 相关功能：竞赛列表、比赛、我的竞赛、竞赛排名



create table tb_user_exam(
    user_exam_id bigint unsigned not null comment 'id主键',
    user_id bigint unsigned not null comment '用户id',
    exam_id bigint unsigned not null comment '竞赛id',
    score int unsigned comment '分数',
    exam_rank int unsigned comment '排名',
    create_by bigint unsigned not null comment '创建者',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新者',
    update_time datetime comment '更新时间',
    primary key (user_exam_id)
);

- 缓存结构设计：
- 用户竞赛列表：list user:exam:list:{userId} -> examId
  一个用户一个list，存储用户报名的所有竞赛的id
- 竞赛详情：string exam:detail:{examId} -> json字符串详情信息