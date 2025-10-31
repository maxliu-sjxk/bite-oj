package com.bite.system.service.question.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.security.exception.ServiceException;
import com.bite.system.domain.question.Question;
import com.bite.system.domain.question.dto.QuestionAddDTO;
import com.bite.system.domain.question.dto.QuestionEditDTO;
import com.bite.system.domain.question.dto.QuestionQueryDTO;
import com.bite.system.domain.question.es.QuestionES;
import com.bite.system.domain.question.vo.QuestionDetailVO;
import com.bite.system.domain.question.vo.QuestionVO;
import com.bite.system.elasticsearch.QuestionRepository;
import com.bite.system.mapper.question.QuestionMapper;
import com.bite.system.service.question.IQuestionService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements IQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public List<QuestionVO> list(QuestionQueryDTO questionQueryDTO) {
        String excludeIdStr = questionQueryDTO.getExcludeIdStr();
        //传入可选参数：excludeIdStr
        if (StrUtil.isNotEmpty(excludeIdStr)) {
            //分割拿到每个questionId，但仍是String类型
            String[] excludeIds = excludeIdStr.split(Constants.SPLIT_SEM);
            //转换为Long并赋值给questionQueryDTO，然后交由mapper层
            Set<Long> excludeIdSet = Arrays.stream(excludeIds)
                    .map(Long::valueOf).collect(Collectors.toSet());
            questionQueryDTO.setExcludeIdSet(excludeIdSet);
        }
        //自动分页
        PageHelper.startPage(questionQueryDTO.getPageNum(), questionQueryDTO.getPageSize());
        return questionMapper.selectQuestionList(questionQueryDTO);
    }

    @Override
    public boolean add(QuestionAddDTO questionAddDTO) {
        //查询数据库，判断题目是否已经存在
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getTitle, questionAddDTO.getTitle()));
        if (CollectionUtil.isNotEmpty(questions)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        Question question = new Question();
        BeanUtil.copyProperties(questionAddDTO, question);
        int rows = questionMapper.insert(question);
        if (rows <= 0) {
            return false;
        }
        QuestionES questionES = new QuestionES();
        BeanUtil.copyProperties(question, questionES);
        questionRepository.save(questionES);
        return true;
    }

    @Override
    public R<QuestionDetailVO> detail(Long questionId) {
        Question question = selectQuestionById(questionId);
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        BeanUtil.copyProperties(question, questionDetailVO);
        return R.ok(questionDetailVO);
    }

    @Override
    public int edit(QuestionEditDTO questionEditDTO) {
        Question oldQuestion = selectQuestionById(questionEditDTO.getQuestionId());
        BeanUtil.copyProperties(questionEditDTO, oldQuestion);
        QuestionES questionES = new QuestionES();
        BeanUtil.copyProperties(oldQuestion, questionES);
        questionRepository.save(questionES);
        return questionMapper.updateById(oldQuestion);
    }

    @Override
    public int delete(Long questionId) {
        Question question = selectQuestionById(questionId);
        questionRepository.deleteById(questionId);
        return questionMapper.deleteById(questionId);
    }

    private Question selectQuestionById(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return question;
    }


}
