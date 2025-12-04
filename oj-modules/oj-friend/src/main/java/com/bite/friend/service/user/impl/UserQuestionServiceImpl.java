package com.bite.friend.service.user.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.bite.api.apis.RemoteJudgeServiceApi;
import com.bite.api.domain.UserExeResult;
import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ProgramType;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.utils.ThreadLocalUtils;
import com.bite.common.security.exception.ServiceException;
import com.bite.friend.domain.question.Question;
import com.bite.friend.domain.question.QuestionCase;
import com.bite.friend.domain.question.es.QuestionES;
import com.bite.friend.domain.user.UserSubmit;
import com.bite.friend.domain.user.dto.UserSubmitDTO;
import com.bite.api.domain.vo.UserQuestionResultVO;
import com.bite.friend.elasticsearch.QuestionRepository;
import com.bite.friend.mapper.question.QuestionMapper;
import com.bite.friend.mapper.user.UserSubmitMapper;
import com.bite.friend.rabbit.JudgeProducer;
import com.bite.friend.service.user.IUserQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserQuestionServiceImpl implements IUserQuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private RemoteJudgeServiceApi remoteJudgeServiceApi;

    @Autowired
    private JudgeProducer judgeProducer;

    @Autowired
    private UserSubmitMapper userSubmitMapper;


    /**
     * 先判断编程语言是否支持，如果不支持则直接抛异常；如果支持，则继续执行
     * 根据语言类型执行对应的 封装JudgeSubmitDTO对象方法，进而调用oj-judge服务进行判题
     * @param userSubmitDTO
     * @return R<UserQuestionResultVO>
     */
    @Override
    public R<UserQuestionResultVO> submit(UserSubmitDTO userSubmitDTO) {
        Integer programType = userSubmitDTO.getProgramType();
        if (ProgramType.JAVA.getValue().equals(programType)) {
            JudgeSubmitDTO judgeSubmitDTO = assembleJudgeSubmitDTO4J(userSubmitDTO);
            return remoteJudgeServiceApi.doJudgeJavaCode(judgeSubmitDTO);
        } else if (ProgramType.CPP.getValue().equals(programType)) {
            //TODO C++
        } else if (ProgramType.PYTHON.getValue().equals(programType)) {
            //TODO Python
        }
        throw new ServiceException(ResultCode.FAILED_PROGRAM_TYPE_NOT_SUPPORT);
    }

    @Override
    public boolean rabbitSubmit(UserSubmitDTO userSubmitDTO) {
        Integer programType = userSubmitDTO.getProgramType();
        if (ProgramType.JAVA.getValue().equals(programType)) {
            JudgeSubmitDTO judgeSubmitDTO = assembleJudgeSubmitDTO4J(userSubmitDTO);
            judgeProducer.produceMsg(judgeSubmitDTO);
            return true;
        } else if (ProgramType.CPP.getValue().equals(programType)) {
            //TODO C++
        } else if (ProgramType.PYTHON.getValue().equals(programType)) {
            //TODO Python
        }
        throw new ServiceException(ResultCode.FAILED_PROGRAM_TYPE_NOT_SUPPORT);
    }


    /**
     * 流程：
     * 直接查库，封装结果即可
     * @param examId 竞赛id
     * @param questionId 题目id
     * @param currentTime 查询时时间
     * @return UserQuestionResultVO
     */
    @Override
    public UserQuestionResultVO exeResult(Long examId, Long questionId, String currentTime) {
            Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        UserSubmit userSubmit = userSubmitMapper.selectCurrentUserSubmit(userId, examId, questionId, currentTime);
        if (userSubmit == null) {
            return UserQuestionResultVO.judging();
        } else {
            List<UserExeResult> userExeResultList = StrUtil.isNotEmpty(userSubmit.getCaseJudgeRes()) ?
                    JSON.parseArray(userSubmit.getCaseJudgeRes(), UserExeResult.class) : null;
            return UserQuestionResultVO.custom(userSubmit.getPass(), userExeResultList, userSubmit.getExeMessage());
        }
    }


    private JudgeSubmitDTO assembleJudgeSubmitDTO4J(UserSubmitDTO userSubmitDTO) {
        JudgeSubmitDTO judgeSubmitDTO = new JudgeSubmitDTO();
        Long questionId = userSubmitDTO.getQuestionId();
        //查ES
        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
        //为空则查询数据库并刷新ES
        if (questionES == null) {
            Question question = questionMapper.selectById(questionId);
            questionES = new QuestionES();
            //确保questionES不为空
            BeanUtil.copyProperties(question, questionES);
            questionRepository.save(questionES);
        }
        BeanUtil.copyProperties(questionES, judgeSubmitDTO);
        judgeSubmitDTO.setUserId(ThreadLocalUtils.get(Constants.USER_ID, Long.class));
        judgeSubmitDTO.setExamId(userSubmitDTO.getExamId());
        judgeSubmitDTO.setProgramType(userSubmitDTO.getProgramType());
        judgeSubmitDTO.setUserCode(codeConnect(userSubmitDTO.getUserCode(), questionES.getMainFunc()));
        log.info("[questionES]: " + questionES.getMainFunc());
        log.info("[questionES]: " + codeConnect(userSubmitDTO.getUserCode(), questionES.getMainFunc()));
        //将Json数组字符串转换为List
        List<QuestionCase> questionCaseList = JSONUtil.toList(questionES.getQuestionCase(), QuestionCase.class);
        judgeSubmitDTO.setInputList(questionCaseList.stream().map(QuestionCase::getInput).toList());
        judgeSubmitDTO.setOutputList(questionCaseList.stream().map(QuestionCase::getOutput).toList());
        return judgeSubmitDTO;
    }


    private String codeConnect(String userCode, String mainFunc) {
        String targetCharacter = "}";
        int targetLastIndex = userCode.lastIndexOf(targetCharacter);
        if (targetLastIndex != -1) {
            return userCode.substring(0,
                    targetLastIndex) + "\n" + mainFunc + "\n" + userCode.substring(targetLastIndex);
        }
        throw new ServiceException(ResultCode.FAILED);
    }


    //    private JudgeSubmitDTO assembleJudgeSubmitDTO(UserSubmitDTO userSubmitDTO) {
//        JudgeSubmitDTO judgeSubmitDTO = new JudgeSubmitDTO();
//        Long questionId = userSubmitDTO.getQuestionId();
//        //查ES
//        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
//        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
//        if (questionES != null) {
//            BeanUtil.copyProperties(questionES, judgeSubmitDTO);
//        } else {
//            //查询数据库并刷新ES
//            Question question = questionMapper.selectById(questionId);
//            questionES = new QuestionES();
//            BeanUtil.copyProperties(question, questionES);
//            BeanUtil.copyProperties(question, judgeSubmitDTO);
//            questionRepository.save(questionES);
//        }
//    }
}
