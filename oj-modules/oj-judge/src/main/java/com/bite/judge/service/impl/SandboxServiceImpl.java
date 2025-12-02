package com.bite.judge.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.JudgeConstants;
import com.bite.common.core.enums.CodeRunStatus;
import com.bite.judge.callback.DockerStartResultCallback;
import com.bite.judge.callback.StatisticsCallback;
import com.bite.judge.domain.CompileResult;
import com.bite.judge.domain.SandBoxExecuteResult;
import com.bite.judge.service.ISandboxService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class SandboxServiceImpl implements ISandboxService {

    @Value("${sandbox.docker.host:tcp://localhost:2375}")
    private String dockerHost;

    @Value("${sandbox.limit.memory:100000000}")
    private Long memoryLimit;

    @Value("${sandbox.limit.memory-swap:100000000}")
    private Long memorySwapLimit;

    @Value("${sandbox.limit.cpu:1}")
    private Long cpuLimit;

    @Value("${sandbox.limit.time:5}")
    private Long timeLimit;

    private DockerClient dockerClient;

    private String containerId;

    private String userCodeDir;

    private String userCodeFileName;

    /**
     * 1. 将用户代码写入宿主机文件
     * 2. 初始化容器
     *    - 创建 Docker 客户端
     *    - 拉取镜像
     *    - 创建并启动容器
     * 3. 编译用户代码
     *    - 创建编译命令(id)
     *    - 执行编译命令
     *    - 返回编译结果
     * 4. 执行用户代码
     *    - 创建执行命令(id)
     *    - 对于每个测试用例：
     *      - 启动计时器；启动容器情况监控
     *      - 执行当前测试用例
     *      - 关闭计时器；停止容器情况监控
     *      - 更新时间和空间最值
     *      - 追加执行结果
     * 5. 资源释放
     *      - 删除容器
     *      - 清理用户代码文件
     *
     * @param userId
     * @param userCode
     * @param inputList
     * @return
     */
    @Override
    public SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        createUserCodeFile(userId, userCode);
        initDockerSandBox();
        log.info("[initDockerSandBox] success!");
        CompileResult compileResult = compileJavaCodeByDocker();
        log.info("[compileJavaCodeByDocker] success!");
        if (!compileResult.isCompiled()) {
            clearSources();
            return SandBoxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED, compileResult.getExeMessage());
        }
        log.info("[compileResult]: " + compileResult.isCompiled());
        System.out.println("运行吗？" + isContainerRunning(containerId));
        return executeJavaCodeByDocker(inputList);
    }

    private boolean isContainerRunning(String containerId) {
        try {
            InspectContainerResponse response = dockerClient.inspectContainerCmd(containerId).exec();
            return "running".equalsIgnoreCase(response.getState().getStatus());
        } catch (NotFoundException e) {
            return false;
        }
    }



    private void createUserCodeFile(Long userId, String userCode) {
        String examCodeDir = System.getProperty("user.dir") + File.separator + JudgeConstants.EXAM_CODE_DIR;
        if (!FileUtil.exist(examCodeDir)) {
            FileUtil.mkdir(examCodeDir); //创建存放用户代码的目录
        }
        String time = LocalDateTimeUtil.format(LocalDateTime.now(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        //拼接用户代码文件格式
        userCodeDir = examCodeDir + File.separator + userId + Constants.UNDERLINE_SEPARATOR + time;
        userCodeFileName = userCodeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
        FileUtil.writeString(userCode, userCodeFileName, Constants.UTF8);
    }

    private void initDockerSandBox() {
        //配置连接信息
        DefaultDockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
        //创建docker客户端
        dockerClient = DockerClientBuilder
                .getInstance(clientConfig)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
        //拉取镜像
        pullJavaEnvImage();
        //创建容器 限制资源 控制权限
        HostConfig hostConfig = buildHostConfig();
        CreateContainerCmd containerCmd = dockerClient
                .createContainerCmd(JudgeConstants.JAVA_ENV_IMAGE)
                .withName(JudgeConstants.JAVA_CONTAINER_NAME);
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true) //伪终端
                .exec();
        //记录容器id
        containerId = createContainerResponse.getId();
        //启动容器
        dockerClient.startContainerCmd(containerId).exec();
    }

    //拉取java执行环境镜像 需要控制只拉取一次

    private void pullJavaEnvImage() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            if (repoTags != null && repoTags.length > 0 && JudgeConstants.JAVA_ENV_IMAGE.equals(repoTags[0])) {
                return;
            }
        }
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JudgeConstants.JAVA_ENV_IMAGE);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
//    private void pullJavaEnvImage() {
//        if (isImageExists(JudgeConstants.JAVA_ENV_IMAGE)) {
//            log.info("镜像 {} 已存在，跳过拉取。", JudgeConstants.JAVA_ENV_IMAGE);
//            return;
//        }
//        // 1. 创建可取消的Future
//        CompletableFuture<Void> pullFuture = CompletableFuture.runAsync(() -> {
//            try {
//                dockerClient.pullImageCmd(JudgeConstants.JAVA_ENV_IMAGE)
//                        .exec(new PullImageResultCallback() {
//                            @Override
//                            public void onNext(PullResponseItem item) {
//                                log.debug("进度: {}", item.getStatus());
//                                super.onNext(item);
//                            }
//                        })
//                        .awaitCompletion();
//                log.info("镜像拉取成功");
//            } catch (InterruptedException e) {
//                // 响应取消请求
//                log.warn("镜像拉取被取消");
//                Thread.currentThread().interrupt();
//            }
//        });
//
//        try {
//            // 2. 带超时的等待
//            pullFuture.get(10, TimeUnit.MINUTES);
//        } catch (TimeoutException e) {
//            // 3. 超时后取消任务
//            log.error("拉取镜像超时，正在取消...");
//            pullFuture.cancel(true); // true表示尝试中断工作线程
//            // 等待一小段时间让任务响应取消
//            try {
//                pullFuture.get(2, TimeUnit.SECONDS);
//            } catch (CancellationException ce) {
//                log.info("拉取任务已成功取消");
//            } catch (Exception ae) {
//                log.error("拉取任务取消失败");
//            }
//            log.error("镜像拉取超时，请检查网络或Docker仓库状态");
//            throw new RuntimeException();
//        } catch (ExecutionException e) {
//            log.error("镜像拉取失败: " + e.getCause().getMessage());
//            throw new RuntimeException();
//        } catch (InterruptedException e) {
//            // 处理主等待被中断的情况
//            Thread.currentThread().interrupt();
//            log.warn("镜像拉取等待被中断");
//            throw new RuntimeException();
//        }
//    }
//
//    private boolean isImageExists(String targetImageName) {
//        List<Image> images = dockerClient.listImagesCmd().exec();
//        for (Image image : images) {
//            String[] repoTags = image.getRepoTags();
//            if (repoTags != null) {
//                for (String tag : repoTags) {
//                    if (targetImageName.equals(tag)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    //限制资源   控制权限
    private HostConfig buildHostConfig() {
        HostConfig hostConfig = new HostConfig();
        //设置挂载目录，指定用户代码路径
        hostConfig.setBinds(new Bind(userCodeDir, new Volume(JudgeConstants.DOCKER_USER_CODE_DIR)));
        //限制docker容器使用资源
        hostConfig.withMemory(memoryLimit);
        hostConfig.withMemorySwap(memorySwapLimit);
        hostConfig.withCpuCount(cpuLimit);
        hostConfig.withNetworkMode("none");  //禁用网络
        hostConfig.withReadonlyRootfs(true); //禁止在root目录写文件
        return hostConfig;
    }

    private CompileResult compileJavaCodeByDocker() {
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

    private SandBoxExecuteResult executeJavaCodeByDocker(List<String> inputList) {
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
        clearSources();
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

    private void deleteContainer() {
        //执行完成之后删除容器
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
        //断开和docker连接
        try {
            dockerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteUserCodeFile() {
        FileUtil.del(userCodeDir);
    }

    private void clearSources() {
        deleteContainer();//删除容器
        deleteUserCodeFile(); //清理文件
    }
}

//[{"input": "1 2", "output": "3"}, {"input": "4 2", "output": "6"}]
