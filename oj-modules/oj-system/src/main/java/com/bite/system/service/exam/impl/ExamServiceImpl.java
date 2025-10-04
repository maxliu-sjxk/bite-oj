package com.bite.system.service.exam.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.security.exception.ServiceException;
import com.bite.system.domain.exam.Exam;
import com.bite.system.domain.exam.ExamQuestion;
import com.bite.system.domain.exam.dto.ExamAddDTO;
import com.bite.system.domain.exam.dto.ExamEditDTO;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.dto.ExamQuestionAddDTO;
import com.bite.system.domain.exam.vo.ExamDetailVO;
import com.bite.system.domain.exam.vo.ExamVO;
import com.bite.system.domain.question.Question;
import com.bite.system.domain.question.vo.QuestionVO;
import com.bite.system.mapper.exam.ExamMapper;
import com.bite.system.mapper.exam.ExamQuestionMapper;
import com.bite.system.mapper.question.QuestionMapper;
import com.bite.system.service.exam.IExamService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ExamServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements IExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamQuestionMapper examQuestionMapper;


    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    @Override
    public String add(ExamAddDTO examAddDTO) {
        checkExamSaveInfo(examAddDTO, null);
        Exam exam = new Exam();
        BeanUtil.copyProperties(examAddDTO, exam);
        examMapper.insert(exam);
        return exam.getExamId().toString();
    }

    private void checkExamSaveInfo(ExamAddDTO examSaveDTO, Long examId) {
        //竞赛标题不可重复
        //添加
        //编辑：用户可能只修改竞赛时间信息（竞赛标题已存在），此时需要排除竞赛标题已存在的情况，否则后续更新操作不会执行
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examSaveDTO.getTitle())
                .ne(examId != null, Exam::getExamId, examId));
        if (CollectionUtil.isNotEmpty(exams)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        //竞赛开始时间不能早于当前时间
        if (examSaveDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_TOO_EARLY);
        }
        //竞赛开始时间不能晚于结束时间
        if (examSaveDTO.getStartTime().isAfter(examSaveDTO.getEndTime())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_TOO_LATE);
        }
    }

    @Override
    public boolean questionAdd(ExamQuestionAddDTO examQuestionAddDTO) {
        //1. 查看竞赛是否存在
        Exam exam = getExam(examQuestionAddDTO.getExamId());
        //检查竞赛是否可编辑（即是否开赛）
        checkExamStarted(exam);
        Set<Long> questionIdSet = examQuestionAddDTO.getQuestionIdSet();
        //用户没有添加任何新题目
        if (CollectionUtil.isEmpty(questionIdSet)) {
            return true;
        }

        //2. 根据题目id集合批量查询出对应的题目
        List<Question> questions = questionMapper.selectBatchIds(questionIdSet);
        if (CollectionUtil.isEmpty(questions) || questions.size() < questionIdSet.size()) {
            throw new ServiceException(ResultCode.EXAM_QUESTION_NOT_EXISTS);
        }

        //3. 构造ExamQuestion并批量插入
        return saveExamQuestion(exam, questions);
    }

    @Override
    public ExamDetailVO detail(Long examId) {
        //1. 检查竞赛是否存在并在存在时返回竞赛的详细信息，之后将竞赛的基本信息封装VO对象中到
        ExamDetailVO examDetailVO = new ExamDetailVO();
        Exam exam = getExam(examId);
        BeanUtil.copyProperties(exam, examDetailVO);
        //查询竞赛下的题目
        //考虑question_order，<可以无需查询该字段，确保结果集按照升序排序即可>
        List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, exam.getExamId()).orderByAsc(ExamQuestion::getQuestionOrder));
        //竞赛可能无题目（系统允许用户预创建只包含竞赛基本信息的竞赛），因此需要判断竞赛是否包含题目，如果没有题目直接返回，无需执行
        //后续逻辑
        if (CollectionUtil.isEmpty(examQuestionList)) {
            return examDetailVO;
        }
        //将题目id转换为单独集合
        List<Long> questionIdList = examQuestionList.stream().map(ExamQuestion::getQuestionId).toList();
        //根据题目id批量查询出对应的题目的详细信息
        //优化：只查询需要的字段
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .select(Question::getQuestionId, Question::getTitle, Question::getDifficulty)
                .in(Question::getQuestionId, questionIdList));
        //将questionList转换为VOList
        List<QuestionVO> questionVOList = BeanUtil.copyToList(questionList, QuestionVO.class);
        examDetailVO.setExamQuestionList(questionVOList);
        return examDetailVO;
    }

    @Override
    public int edit(ExamEditDTO examEditDTO) {
        //验证竞赛是否存在
        Exam exam = getExam(examEditDTO.getExamId());
        //检查竞赛是否可编辑（即是否开赛）
        checkExamStarted(exam);
        //验证修改后的竞赛信息是否合法：1. 竞赛标题不能重复 2. 竞赛起始时间符合常理
        checkExamSaveInfo(examEditDTO, examEditDTO.getExamId());
        exam.setTitle(examEditDTO.getTitle());
        exam.setStartTime(examEditDTO.getStartTime());
        exam.setEndTime(examEditDTO.getEndTime());
        return examMapper.updateById(exam);
    }

    @Override
    public int questionDelete(Long examId, Long questionId) {
        //检查竞赛是否存在
        Exam exam = getExam(examId);
        //检查竞赛是否可编辑（即是否开赛）
        checkExamStarted(exam);
        return examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId));
    }

    private void checkExamStarted(Exam exam) {
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_ALREADY_STARTED);
        }
    }

    private boolean saveExamQuestion(Exam exam, List<Question> questions) {
        List<ExamQuestion> examQuestionList = new ArrayList<>();
        int num = 1;
        for (Question question : questions) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(exam.getExamId());
            examQuestion.setQuestionId(question.getQuestionId());
            examQuestion.setQuestionOrder(num++);
            examQuestionList.add(examQuestion);
        }
        return saveBatch(examQuestionList);
    }

    private Exam getExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return exam;
    }
}
