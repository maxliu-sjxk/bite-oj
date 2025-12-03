package com.bite.judge.service.impl;


import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.JudgeConstants;
import com.bite.common.core.enums.CodeRunStatus;
import com.bite.judge.callback.DockerStartResultCallback;
import com.bite.judge.callback.StatisticsCallback;
import com.bite.judge.config.DockerSandBoxPool;
import com.bite.judge.domain.CompileResult;
import com.bite.judge.domain.SandBoxExecuteResult;
import com.bite.judge.service.ISandboxPoolService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * 参数传递 解决 成员变量：containerId & userCodeFileName 可能导致的线程安全问题
 */
@Slf4j
@Service
public class ParamPassingSandboxPoolServiceImpl implements ISandboxPoolService {

    @Value("${sandbox.limit.time:5}")
    private Long timeLimit;

    @Autowired
    private DockerClient dockerClient;

    @Autowired
    private DockerSandBoxPool dockerSandBoxPool;


    @Override
    public SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        String containerId = dockerSandBoxPool.getContainer();
        String userCodeFileName = createUserCodeFile(userCode, containerId);
        CompileResult compileResult = compileJavaCodeByDocker(containerId);
        if (!compileResult.isCompiled()) {
            dockerSandBoxPool.returnContainer(containerId);
            deleteUserCodeFile(userCodeFileName);
            return SandBoxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED, compileResult.getExeMessage());
        }
        return executeJavaCodeByDocker(inputList, containerId, userCodeFileName);
    }



    private String createUserCodeFile(String userCode, String containerId) {
        String codeDir = dockerSandBoxPool.getCodeDir(containerId);
        String userCodeFileName = codeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
        if (FileUtil.exist(userCodeFileName)) {
            FileUtil.del(userCodeFileName);
        }
        FileUtil.writeString(userCode, userCodeFileName, Constants.UTF8);
        return userCodeFileName;
    }


    private CompileResult compileJavaCodeByDocker(String containerId) {
        String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVAC_CMD, null, containerId);
        DockerStartResultCallback resultCallback = new DockerStartResultCallback();
        CompileResult compileResult = new CompileResult();
        try {
            dockerClient.execStartCmd(cmdId)
                    .exec(resultCallback)
                    .awaitCompletion();
            if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                compileResult.setCompiled(false);
                compileResult.setExeMessage(resultCallback.getErrorMessage());
            } else {
                compileResult.setCompiled(true);
            }
            return compileResult;
        } catch (InterruptedException e) {
            //此处可以直接抛出 已做统一异常处理  也可再做定制化处理
            throw new RuntimeException(e);
        }
    }

    private SandBoxExecuteResult executeJavaCodeByDocker(List<String> inputList,
                                                         String containerId, String userCodeFileName) {
        List<String> outList = new ArrayList<>(); //记录输出结果
        long maxMemory = 0L;  //最大占用内存
        long maxUseTime = 0L; //最大运行时间
        //执行用户代码
        for (String inputArgs : inputList) {
            //创建执行命令
            String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArgs, containerId);

            //执行代码时间监控 执行情况监控
            StopWatch stopWatch = new StopWatch();
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            StatisticsCallback statisticsCallback = statsCmd.exec(new StatisticsCallback());
            stopWatch.start();
            DockerStartResultCallback resultCallback = new DockerStartResultCallback();

            try {
                dockerClient.execStartCmd(cmdId)
                        .exec(resultCallback)
                        .awaitCompletion(timeLimit, TimeUnit.SECONDS);
                if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                    //内部执行出错
                    return SandBoxExecuteResult.fail(CodeRunStatus.FAILED);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            stopWatch.stop();  //结束时间统计
            statsCmd.close();  //结束docker容器执行统计
            long userTime = stopWatch.getLastTaskTimeMillis(); //执行耗时
            maxUseTime = Math.max(userTime, maxUseTime);       //记录最大的执行用例耗时
            Long memory = statisticsCallback.getMaxMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, statisticsCallback.getMaxMemory()); //记录最大的执行用例占用内存
            }
            outList.add(resultCallback.getMessage().trim());   //记录正确的输出结果
        }
        dockerSandBoxPool.returnContainer(containerId);
        deleteUserCodeFile(userCodeFileName);
        return getSandBoxResult(inputList, outList, maxMemory, maxUseTime); //封装结果
    }

    private String createExecCmd(String[] javaCmdArr, String inputArgs, String containerId) {
        if (!StrUtil.isEmpty(inputArgs)) {
            //当入参不为空时拼接入参
            String[] inputArray = inputArgs.split(" "); //入参
            javaCmdArr = ArrayUtil.append(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArray);
        }
        log.info("javaCmd: " + Arrays.toString(javaCmdArr));
        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(javaCmdArr)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        return cmdResponse.getId();
    }

    private SandBoxExecuteResult getSandBoxResult(List<String> inputList, List<String> outList,
                                                  long maxMemory, long maxUseTime) {
        if (inputList.size() != outList.size()) {
            //输入用例数量 不等于 输出用例数量  属于执行异常
            return SandBoxExecuteResult.fail(CodeRunStatus.UNKNOWN_FAILED, outList, maxMemory, maxUseTime);
        }
        return SandBoxExecuteResult.success(CodeRunStatus.SUCCEED, outList, maxMemory, maxUseTime);
    }


    private void deleteUserCodeFile(String userCodeFileName) {
        FileUtil.del(userCodeFileName);
    }
}
