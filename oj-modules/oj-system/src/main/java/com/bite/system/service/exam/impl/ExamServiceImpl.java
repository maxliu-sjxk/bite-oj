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
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.dto.ExamQuestionAddDTO;
import com.bite.system.domain.exam.vo.ExamVO;
import com.bite.system.domain.question.Question;
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
    public int add(ExamAddDTO examAddDTO) {
        //竞赛标题不可重复
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getTitle, examAddDTO.getTitle()));
        if (CollectionUtil.isNotEmpty(exams)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        //竞赛开始时间不能早于当前时间
        if (examAddDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_TOO_EARLY);
        }
        //竞赛开始时间不能晚于结束时间
        if (examAddDTO.getStartTime().isAfter(examAddDTO.getEndTime())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_TOO_LATE);
        }
        Exam exam = new Exam();
        BeanUtil.copyProperties(examAddDTO, exam);
        return examMapper.insert(exam);
    }

    @Override
    public boolean questionAdd(ExamQuestionAddDTO examQuestionAddDTO) {
        //1. 查看竞赛是否存在
        Exam exam = getExam(examQuestionAddDTO.getExamId());

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
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        return exam;
    }
}
