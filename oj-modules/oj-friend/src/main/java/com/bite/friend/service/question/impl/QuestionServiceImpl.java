package com.bite.friend.service.question.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.question.Question;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;
import com.bite.friend.domain.question.es.QuestionES;
import com.bite.friend.domain.question.vo.QuestionDetailVO;
import com.bite.friend.domain.question.vo.QuestionVO;
import com.bite.friend.elasticsearch.QuestionRepository;
import com.bite.friend.manager.QuestionCacheManager;
import com.bite.friend.mapper.question.QuestionMapper;
import com.bite.friend.service.question.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements IQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionCacheManager questionCacheManager;


    /**
     * 流程记录：
     * 先查询es，如果es未命中则查询数据库并刷新es
     * 之后查询es并返回
     * @param questionQueryDTO
     * @return
     */
    @Override
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        //查询es
        long count = questionRepository.count();
        if (count <= 0) {
            //es未命中
            if (!refreshES()) {
                return TableDataInfo.empty();
            }
        }
        Integer difficulty = questionQueryDTO.getDifficulty();
        String keyword = questionQueryDTO.getKeyword();
        Page<QuestionES> questionESPage;
        Pageable pageable = PageRequest.of(questionQueryDTO.getPageNum() - 1,
                questionQueryDTO.getPageSize(), Sort.by(Sort.Direction.DESC, "createTime"));
        //进行es查询
        if (difficulty == null && StrUtil.isEmpty(keyword)) {
            //无筛选条件
            questionESPage = questionRepository.findAll(pageable);
        } else if (difficulty == null) {
            //无难度筛选条件，但存在关键字筛选条件
            questionESPage = questionRepository.findByTitleOrContent(keyword, keyword, pageable);
        } else if (StrUtil.isEmpty(keyword)) {
            //无关键字筛选条件，但存在难度筛选条件
            questionESPage = questionRepository.findQuestionByDifficulty(difficulty, pageable);
        } else {
            questionESPage = questionRepository.findByTitleOrContentAndDifficulty(keyword, keyword,
                    difficulty, pageable);
        }

        long total = questionESPage.getTotalElements();
        //结果集为空
        if (total <= 0) {
            return TableDataInfo.empty();
        }
        List<QuestionES> questionESList = questionESPage.getContent();
        List<QuestionVO> questionVOList = BeanUtil.copyToList(questionESList, QuestionVO.class);
        return TableDataInfo.success(questionVOList, total);
    }

    @Override
    public QuestionDetailVO detail(Long questionId) {
        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        if (questionES != null) {
            BeanUtil.copyProperties(questionES, questionDetailVO);
            return questionDetailVO;
        }
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            return null;
        }
        refreshES();
        BeanUtil.copyProperties(question, questionDetailVO);
        return questionDetailVO;
    }

    /**
     * 分析：需要维护一个题目列表，该列表顺序根据创建时间维护
     * 走 Redis
     * 1. 查缓存：
     * - 缓存未命中，则查库并刷新缓存
     * - 缓存命中，直接调用返回
     *
     * @param questionId
     * @return 上一题的题目id
     */
    @Override
    public String preQuestion(Long questionId) {
        checkAndRefreshQuestionCache();
        return questionCacheManager.preQuestion(questionId).toString();
    }

    @Override
    public String nextQuestion(Long questionId) {
        //查缓存
        checkAndRefreshQuestionCache();
        return questionCacheManager.nextQuestion(questionId).toString();
    }

    /**
     * 查询数据库，将数据同步给ES
     * @return 刷新成功返回true；刷新失败返回false，方便后续处理，避免无效的es查询
     */
    private boolean refreshES() {
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>());
        if (CollectionUtil.isEmpty(questionList)) {
            return false;
        }
        List<QuestionES> questionESList = BeanUtil.copyToList(questionList, QuestionES.class);
        questionRepository.saveAll(questionESList);
        return true;
    }

    private void checkAndRefreshQuestionCache() {
        //查缓存
        Long listSize = questionCacheManager.getQuestionListSize();
        if (listSize == null || listSize <= 0) {
            //刷新缓存
            questionCacheManager.refreshCache();
        }
    }
}
