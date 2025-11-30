package com.bite.judge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.api.domain.UserExeResult;
import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.api.domain.vo.UserQuestionResultVO;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.JudgeConstants;
import com.bite.common.core.enums.CodeRunStatus;
import com.bite.judge.domain.SandBoxExecuteResult;
import com.bite.judge.domain.UserSubmit;
import com.bite.judge.mapper.UserSubmitMapper;
import com.bite.judge.service.IJudgeService;
import com.bite.judge.service.ISandboxService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JudgeServiceImpl implements IJudgeService {


    @Resource(name = "sandboxServiceImpl")
    private ISandboxService sandboxService;

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    /**
     * 先调用sandboxService服务执行代码（代码 + 测试用例输入）
     * 判断代码执行结果：
     * - 空值：未知异常
     * - 代码执行无误
     *   - 检查测试用例
     *   - 检查内存消耗
     *   - 检查时间消耗
     * - 代码执行有误
     *   - 返回异常信息
     * 提交记录数据入库（只保留最近一次）：先删除旧数据，再保存新数据
     *
     * @param judgeSubmitDTO
     * @return UserQuestionResultVO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO) {
        UserQuestionResultVO userQuestionResultVO;
        SandBoxExecuteResult sandBoxExecuteResult = sandboxService.exeJavaCode(judgeSubmitDTO.getUserCode(),
                judgeSubmitDTO.getInputList());
        if (sandBoxExecuteResult == null) {
            userQuestionResultVO = UserQuestionResultVO.fail(CodeRunStatus.UNKNOWN_FAILED.getMsg());
        }

        if (CodeRunStatus.SUCCEED.equals(sandBoxExecuteResult.getRunStatus())) {
            //代码执行成功，但需要检查结果以及时间空间消耗
            userQuestionResultVO = doJudge(judgeSubmitDTO, sandBoxExecuteResult);

        } else {
            //代码执行失败，返回异常信息
            userQuestionResultVO = UserQuestionResultVO.fail(sandBoxExecuteResult.getExeMessage());
        }
        saveUserSubmit(judgeSubmitDTO, userQuestionResultVO);
        return userQuestionResultVO;
    }

    private void saveUserSubmit(JudgeSubmitDTO judgeSubmitDTO, UserQuestionResultVO userQuestionResultVO) {
        try {
            UserSubmit userSubmit = buildUserSubmit(judgeSubmitDTO, userQuestionResultVO);
            //TODO 需要事务
            int deleteCount = userSubmitMapper.delete(new LambdaQueryWrapper<UserSubmit>()
                    .eq(UserSubmit::getUserId, userSubmit.getUserId())
                    .eq(UserSubmit::getQuestionId, userSubmit.getQuestionId())
                    .isNull(judgeSubmitDTO.getExamId() == null, UserSubmit::getExamId)
                    .eq(judgeSubmitDTO.getExamId() != null, UserSubmit::getExamId, judgeSubmitDTO.getExamId()));
            log.debug("删除用户 {} 题目 {} 的历史提交记录 {} 条",
                    userSubmit.getUserId(), userSubmit.getQuestionId(), deleteCount);
            userSubmitMapper.insert(userSubmit);
        } catch (Exception e) {
            log.error("保存用户提交记录失败: userId={}, questionId={}",
                    judgeSubmitDTO.getUserId(), judgeSubmitDTO.getQuestionId(), e);
            throw new RuntimeException("保存提交记录失败", e);
        }

    }

    private UserSubmit buildUserSubmit(JudgeSubmitDTO judgeSubmitDTO, UserQuestionResultVO userQuestionResultVO) {
        UserSubmit userSubmit = new UserSubmit();
        BeanUtil.copyProperties(judgeSubmitDTO, userSubmit);
        userSubmit.setPass(userQuestionResultVO.getPass());
        userSubmit.setScore(userQuestionResultVO.getScore());
        userSubmit.setExeMessage(userQuestionResultVO.getExeMessage());
        return userSubmit;
    }

    private UserQuestionResultVO doJudge(JudgeSubmitDTO judgeSubmitDTO, SandBoxExecuteResult sandBoxExecuteResult) {
        List<String> outputList = judgeSubmitDTO.getOutputList();
        List<String> exeOutputList = sandBoxExecuteResult.getOutputList();
        List<String> inputList = judgeSubmitDTO.getInputList();
        if (outputList.size() != exeOutputList.size()) {
            return UserQuestionResultVO.fail(JudgeConstants.ERROR_ANSWER);
        }
        List<UserExeResult> userExeResultList = new ArrayList<>();
        boolean isPass = true;
        for (int i = 0; i < outputList.size(); i++) {
            String output = outputList.get(i);
            String exeOutput = exeOutputList.get(i);
            String input = inputList.get(i);
            if (!output.equals(exeOutput)) {
                isPass = false;
            }
            UserExeResult userExeResult = new UserExeResult();
            userExeResult.setInput(input);
            userExeResult.setOutput(output);
            userExeResult.setExeOutput(exeOutput);
            userExeResultList.add(userExeResult);
        }
        if (!isPass) {
            return UserQuestionResultVO.fail(JudgeConstants.ERROR_ANSWER);
        }
        if (sandBoxExecuteResult.getUseMemory() > judgeSubmitDTO.getSpaceLimit()) {
            return UserQuestionResultVO.fail(JudgeConstants.OUT_OF_MEMORY);
        }
        if (sandBoxExecuteResult.getUseTime() > judgeSubmitDTO.getTimeLimit()) {
            return UserQuestionResultVO.fail(JudgeConstants.OUT_OF_TIME);
        }
        return UserQuestionResultVO.success(userExeResultList, judgeSubmitDTO.getDifficulty());
    }
}
