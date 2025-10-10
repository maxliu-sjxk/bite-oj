
-- tb_question
INSERT INTO
`tb_question` (`question_id`, `title`, `content`, `difficulty`, `time_limit`, `space_limit`, `question_case`, `default_code`, `main_func`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, '题目1', '描述1', 1, 12, 256, '{xxx}', 'code', 'code', 1, '2025-09-16 10:44:20', NULL, NULL);

INSERT INTO
`tb_question` (`question_id`, `title`, `content`, `difficulty`, `time_limit`, `space_limit`, `question_case`, `default_code`, `main_func`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, '两数之和', '描述2', 1, 12, 256, '{xxx}', 'code', 'code', 1, '2025-09-16 10:50:20', NULL, NULL);

INSERT INTO
`tb_question` (`question_id`, `title`, `content`, `difficulty`, `time_limit`, `space_limit`, `question_case`, `default_code`, `main_func`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (3, '字母异位词分组', '描述3', 2, 12, 256, '{xxx}', 'code', 'code', 1, '2025-09-16 11:50:20', NULL, NULL);

INSERT INTO
`tb_question` (`question_id`, `title`, `content`, `difficulty`, `time_limit`, `space_limit`, `question_case`, `default_code`, `main_func`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (4, '最长连续序列', '描述4', 2, 12, 256, '{xxx}', 'code', 'code', 1, '2025-09-17 10:44:20', NULL, NULL);

INSERT INTO
`tb_question` (`question_id`, `title`, `content`, `difficulty`, `time_limit`, `space_limit`, `question_case`, `default_code`, `main_func`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (5, '移动零', '描述5', 1, 12, 256, '{xxx}', 'code', 'code', 1, '2025-09-15 10:44:20', NULL, NULL);

INSERT INTO
`tb_question` (`question_id`, `title`, `content`, `difficulty`, `time_limit`, `space_limit`, `question_case`, `default_code`, `main_func`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (6, '接雨水', '描述6', 3, 12, 256, '{xxx}', 'code', 'code', 1, '2025-09-18 10:44:20', NULL, NULL);


INSERT INTO
`tb_question` (`question_id`, `title`, `content`, `difficulty`, `time_limit`, `space_limit`, `question_case`, `default_code`, `main_func`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (7, '最小覆盖子串', '描述7', 3, 12, 256, '{xxx}', 'code', 'code', 1, '2025-09-19 10:44:20', NULL, NULL);

-- tb_exam
INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (1, '竞赛001', '2025-09-20 18:11:05', '2025-10-24 18:11:08', 0, 1, '2025-09-19 18:11:27', NULL, NULL);

INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (2, '竞赛002', '2025-09-21 18:11:05', '2025-10-25 18:11:08', 0, 1, '2025-09-17 18:11:27', NULL, NULL);

INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (3, '竞赛003', '2025-09-30 18:11:05', '2025-11-24 18:10:08', 0, 1, '2025-09-19 18:11:27', NULL, NULL);

INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (4, '竞赛004', '2025-09-20 17:11:05', '2025-10-24 18:11:08', 0, 1, '2025-09-20 18:11:27', NULL, NULL);

INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (5, '竞赛005', '2026-09-20 18:11:05', '2026-10-24 18:11:08', 0, 1, '2025-09-19 18:11:27', NULL, NULL);

INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (6, '竞赛006', '2025-09-20 19:11:05', '2025-10-24 18:11:08', 0, 1, '2025-09-19 17:11:27', NULL, NULL);

INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (7, '竞赛007', '2025-09-20 20:11:05', '2025-10-25 18:11:08', 0, 1, '2025-09-12 17:11:27', NULL, NULL);

-- tb_user

INSERT INTO `tb_user` (`user_id`, `nick_name`, `head_image`, `sex`, `phone`, `code`, `email`, `wechat`, `school_name`, `major_name`, `introduce`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (100001, '张三', NULL, 1, '13253234214', NULL, 'zhangsan@qq.com', 'zhangsan13253234214', 'xxx大学', '软件工程', NULL, 1, 100001, '2025-10-10 08:20:05', NULL, NULL);

INSERT INTO `tb_user` (`user_id`, `nick_name`, `head_image`, `sex`, `phone`, `code`, `email`, `wechat`, `school_name`, `major_name`, `introduce`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (100002, '李四', NULL, 2, '13233234214', NULL, 'lisi@qq.com', 'lisi13233234214', 'xxx大学', '汉语言文学', NULL, 1, 100001, '2025-10-09 08:20:05', NULL, NULL);

INSERT INTO `tb_user` (`user_id`, `nick_name`, `head_image`, `sex`, `phone`, `code`, `email`, `wechat`, `school_name`, `major_name`, `introduce`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (100003, '王五', NULL, 1, '13253234255', NULL, 'wangwu@qq.com', 'wangwu13253234255', 'xxx大学', '计算机科学与技术', NULL, 0, 100001, '2025-10-08 08:20:05', NULL, NULL);

-- 用户ID 100004
INSERT INTO `tb_user` (`user_id`, `nick_name`, `head_image`, `sex`, `phone`, `code`, `email`, `wechat`, `school_name`, `major_name`, `introduce`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (100004, '赵六', NULL, 1, '13876543210', NULL, 'zhaoliu@qq.com', 'zhaoliu13876543210', 'yyy大学', '机械工程', NULL, 1, 100004, '2025-10-07 15:30:22', NULL, NULL);

-- 用户ID 100005
INSERT INTO `tb_user` (`user_id`, `nick_name`, `head_image`, `sex`, `phone`, `code`, `email`, `wechat`, `school_name`, `major_name`, `introduce`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (100005, '孙七', NULL, 2, '13987654321', NULL, 'sunqi@qq.com', 'sunqi13987654321', 'yyy大学', '会计学', NULL, 1, 100005, '2025-10-06 09:15:47', NULL, NULL);

-- 用户ID 100006
INSERT INTO `tb_user` (`user_id`, `nick_name`, `head_image`, `sex`, `phone`, `code`, `email`, `wechat`, `school_name`, `major_name`, `introduce`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (100006, '周八', NULL, 1, '13567890123', NULL, 'zhouba@qq.com', 'zhouba13567890123', 'zzz大学', '临床医学', NULL, 0, 100006, '2025-10-05 16:42:18', NULL, NULL);

-- 用户ID 100007
INSERT INTO `tb_user` (`user_id`, `nick_name`, `head_image`, `sex`, `phone`, `code`, `email`, `wechat`, `school_name`, `major_name`, `introduce`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (100007, '吴九', NULL, 2, '13654321098', NULL, 'wujiu@qq.com', 'wujiu13654321098', 'zzz大学', '教育学', NULL, 1, 100007, '2025-10-04 11:28:33', NULL, NULL);

-- 用户ID 100008
INSERT INTO `tb_user` (`user_id`, `nick_name`, `head_image`, `sex`, `phone`, `code`, `email`, `wechat`, `school_name`, `major_name`, `introduce`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (100008, '郑十', NULL, 1, '13789012345', NULL, 'zhengshi@qq.com', 'zhengshi13789012345', 'xxx大学', '金融学', NULL, 1, 100008, '2025-10-03 14:55:09', NULL, NULL);