

-- 消息内容表
create table tb_message_text(
    text_id bigint unsigned NOT NULL COMMENT '消息内容id（主键）',
    message_title varchar(10) NOT NULL COMMENT '消息标题',
    message_content varchar(200) NOT NULL COMMENT '消息内容',
    create_by bigint unsigned not null comment '创建人',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新人',
    update_time datetime comment '更新时间',
    primary key (text_id)
);



-- 消息表
create table tb_message(
    message_id bigint unsigned NOT NULL COMMENT '消息id（主键）',
    text_id bigint unsigned NOT NULL COMMENT '消息内容id（主键）',
    send_id bigint unsigned NOT NULL COMMENT '消息发送人id',
    rec_id  bigint unsigned NOT NULL COMMENT '消息接收人id',
    create_by bigint unsigned not null comment '创建人',
    create_time datetime not null comment '创建时间',
    update_by bigint unsigned comment '更新人',
    update_time datetime comment '更新时间',
    primary key (message_id)
)