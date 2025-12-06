
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

--  测试统计定时任务
-- mock竞赛
INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (11, '竞赛统计测试01', '2025-12-05 18:00:00', '2025-12-05 20:00:00', 1, 1, '2025-09-19 18:11:27', NULL, NULL);

INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (12, '竞赛统计测试02', '2025-12-05 19:00:00', '2025-12-05 20:30:00', 1, 1, '2025-09-19 17:11:27', NULL, NULL);

INSERT INTO
`tb_exam` (`exam_id`, `title`, `start_time`, `end_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES (13, '竞赛统计测试03', '2025-12-05 20:30:05', '2025-12-05 21:11:08', 1, 1, '2025-09-12 17:11:27', NULL, NULL);

-- xxxxxxxxxxxxxxxxxxxxxxx
INSERT INTO tb_user_exam
 (user_exam_id, user_id, exam_id, score, exam_rank, create_by, create_time, update_by, update_time)
 VALUES(1838792223446659073, 1977348903870078978, 11, NULL, NULL, 1977348903870078978, '2024-12-04 12:06:44', NULL, NULL);

INSERT INTO tb_user_exam
(user_exam_id, user_id, exam_id, score, exam_rank, create_by, create_time, update_by, update_time)
VALUES(1838792223446659074, 100001, 11, NULL, NULL, 100001, '2024-12-04 12:06:44', NULL, NULL);

INSERT INTO tb_user_exam
(user_exam_id, user_id, exam_id, score, exam_rank, create_by, create_time, update_by, update_time)
VALUES(1838792223446659075, 100002, 11, NULL, NULL, 100002, '2024-12-04 12:06:44', NULL, NULL);

INSERT INTO tb_user_exam
(user_exam_id, user_id, exam_id, score, exam_rank, create_by, create_time, update_by, update_time)
VALUES(1838792223446959075, 100003, 11, NULL, NULL, 100003, '2024-12-04 12:06:44', NULL, NULL);

INSERT INTO tb_user_exam
(user_exam_id, user_id, exam_id, score, exam_rank, create_by, create_time, update_by, update_time)
VALUES(1838792226839851009, 1977348903870078978, 12, NULL, NULL, 1977348903870078978, '2024-12-04 12:07:44', NULL, NULL);

INSERT INTO tb_user_exam
(user_exam_id, user_id, exam_id, score, exam_rank, create_by, create_time, update_by, update_time)
VALUES(1838792226837851019, 100002, 12, NULL, NULL, 100002, '2024-12-04 12:07:44', NULL, NULL);

INSERT INTO tb_user_exam
(user_exam_id, user_id, exam_id, score, exam_rank, create_by, create_time, update_by, update_time)
VALUES(1838792231059320834, 1977348903870078978, 13, NULL, NULL, 1977348903870078978, '2024-12-04 12:08:44', NULL, NULL);

-- xxxxxxxxxxxx

-- mock 竞赛题目
INSERT INTO tb_exam_question
(exam_question_id, exam_id, question_id, question_order, create_by, create_time, update_by, update_time)
VALUES(1, 11, 2, 1, 1, '2024-12-04 12:07:44', null, null);

INSERT INTO tb_exam_question
(exam_question_id, exam_id, question_id, question_order, create_by, create_time, update_by, update_time)
VALUES(2, 11, 3, 2, 1, '2024-12-04 12:07:44', null, null);

INSERT INTO tb_exam_question
(exam_question_id, exam_id, question_id, question_order, create_by, create_time, update_by, update_time)
VALUES(3, 11, 4, 3, 1, '2024-12-04 12:07:44', null, null);

INSERT INTO tb_exam_question
(exam_question_id, exam_id, question_id, question_order, create_by, create_time, update_by, update_time)
VALUES(4, 12, 2, 1, 1, '2024-12-04 12:07:44', null, null);

INSERT INTO tb_exam_question
(exam_question_id, exam_id, question_id, question_order, create_by, create_time, update_by, update_time)
VALUES(5, 12, 3, 2, 1, '2024-12-04 12:07:44', null, null);

INSERT INTO tb_exam_question
(exam_question_id, exam_id, question_id, question_order, create_by, create_time, update_by, update_time)
VALUES(6, 12, 4, 3, 1, '2024-12-04 12:07:44', null, null);

INSERT INTO tb_exam_question
(exam_question_id, exam_id, question_id, question_order, create_by, create_time, update_by, update_time)
VALUES(7, 13, 2, 1, 1, '2024-12-04 12:07:44', null, null);

-- mock提交记录
INSERT INTO tb_user_submit
 (submit_id, user_id, question_id, exam_id, program_type, user_code, pass, exe_message, score, create_by, create_time, update_by, update_time)
 VALUES(1, 1977348903870078978, 2, 11, 0, '1', 1, NULL, 100, 1977348903870078978, '2025-12-05 19:05:00', NULL, NULL);

 INSERT INTO tb_user_submit
 (submit_id, user_id, question_id, exam_id, program_type, user_code, pass, exe_message, score, create_by, create_time, update_by, update_time)
 VALUES(2, 1977348903870078978, 3, 11, 0, '1', 1, NULL, 200, 1977348903870078978, '2025-12-05 19:06:00', NULL, NULL);

  INSERT INTO tb_user_submit
 (submit_id, user_id, question_id, exam_id, program_type, user_code, pass, exe_message, score, create_by, create_time, update_by, update_time)
 VALUES(3, 1977348903870078978, 4, 11, 0, '1', 1, NULL, 200, 1977348903870078978, '2025-12-05 19:06:00', NULL, NULL);

 INSERT INTO tb_user_submit
 (submit_id, user_id, question_id, exam_id, program_type, user_code, pass, exe_message, score, create_by, create_time, update_by, update_time)
 VALUES(4, 100001, 2, 11, 0, '1', 1, NULL, 100, 100001, '2025-12-05 19:05:00', NULL, NULL);

  INSERT INTO tb_user_submit
 (submit_id, user_id, question_id, exam_id, program_type, user_code, pass, exe_message, score, create_by, create_time, update_by, update_time)
 VALUES(5, 100002, 3, 11, 0, '1', 1, NULL, 200, 100002, '2025-12-05 19:05:00', NULL, NULL);

   INSERT INTO tb_user_submit
 (submit_id, user_id, question_id, exam_id, program_type, user_code, pass, exe_message, score, create_by, create_time, update_by, update_time)
 VALUES(6, 100002, 3, 12, 0, '1', 1, NULL, 200, 100002, '2025-12-05 19:05:00', NULL, NULL);

    INSERT INTO tb_user_submit
 (submit_id, user_id, question_id, exam_id, program_type, user_code, pass, exe_message, score, create_by, create_time, update_by, update_time)
 VALUES(7, 1977348903870078978, 2, 12, 0, '1', 1, NULL, 100, 1977348903870078978, '2025-12-05 19:05:00', NULL, NULL);

     INSERT INTO tb_user_submit
 (submit_id, user_id, question_id, exam_id, program_type, user_code, pass, exe_message, score, create_by, create_time, update_by, update_time)
 VALUES(8, 1977348903870078978, 2, 13, 0, '1', 1, NULL, 100, 1977348903870078978, '2025-12-05 19:05:00', NULL, NULL);