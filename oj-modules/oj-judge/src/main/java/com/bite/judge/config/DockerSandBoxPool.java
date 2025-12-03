package com.bite.judge.config;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.bite.common.core.constants.JudgeConstants;
import com.bite.common.core.enums.ContainerStatus;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class DockerSandBoxPool {

    private DockerClient dockerClient;

    private String sandboxImage;

    private String volumeDir;

    private Long memoryLimit;

    private Long memorySwapLimit;

    private Long cpuLimit;

    private int poolSize;

    private String containerNamePrefix;

    private BlockingQueue<String> containerQueue;

    private Map<String, String> containerNameMap;

    public DockerSandBoxPool(DockerClient dockerClient,
                             String sandboxImage,
                             String volumeDir, Long memoryLimit,
                             Long memorySwapLimit, Long cpuLimit,
                             int poolSize, String containerNamePrefix) {
        this.dockerClient = dockerClient;
        this.sandboxImage = sandboxImage;
        this.volumeDir = volumeDir;
        this.memoryLimit = memoryLimit;
        this.memorySwapLimit = memorySwapLimit;
        this.cpuLimit = cpuLimit;
        this.poolSize = poolSize;
        this.containerQueue = new ArrayBlockingQueue<>(poolSize);
        this.containerNamePrefix = containerNamePrefix;
        this.containerNameMap = new HashMap<>();
    }

    public void initDockerPool() {
        pullJavaEnvImage();
        for (int i = 0; i < poolSize; i++) {
            createContainer(JudgeConstants.JAVA_CONTAINER_NAME
                    + JudgeConstants.CONTAINER_NAME_SEPARATOR + i);
        }
    }

    public String getContainer() {
        try {
            return containerQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }

    public void returnContainer(String containerId) {
        containerQueue.offer(containerId);
    }


    private void pullJavaEnvImage() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            if (repoTags != null && repoTags.length > 0 && sandboxImage.equals(repoTags[0])) {
                return;
            }
        }
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(sandboxImage);
        try {
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createContainer(String containerName) {
        List<Container> containerList = dockerClient.listContainersCmd().withShowAll(true).exec();
        if (!CollectionUtil.isEmpty(containerList)) {
            String names = JudgeConstants.JAVA_CONTAINER_PREFIX + containerName;
            for (Container container : containerList) {
                String[] containerNames = container.getNames();
                if (containerNames != null && containerNames.length > 0 && names.equals(containerNames[0])) {
                    if (ContainerStatus.CREATED.getDesc().equals(container.getState())
                            || ContainerStatus.EXITED.getDesc().equals(container.getState())) {
                        //启动容器
                        dockerClient.startContainerCmd(container.getId()).exec();
                    }
                    containerQueue.add(container.getId());
                    containerNameMap.put(container.getId(), containerName);
                    return;
                }
            }
        }

        HostConfig hostConfig = buildHostConfig(containerName);
        CreateContainerCmd containerCmd = dockerClient
                .createContainerCmd(sandboxImage)
                .withName(containerName);
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true) //伪终端
                .exec();
        String containerId = createContainerResponse.getId();
        //启动容器
        dockerClient.startContainerCmd(containerId).exec();
        //记录容器id
        containerQueue.offer(containerId);
        //记录容器名称与容器id的对饮关系
        containerNameMap.put(containerId, containerName);
    }

    private HostConfig buildHostConfig(String containerName) {
        HostConfig hostConfig = new HostConfig();
        String userCodeDir = createContainerDir(containerName);
        //设置挂载目录，指定用户代码路径
        hostConfig.setBinds(new Bind(userCodeDir, new Volume(volumeDir)));
        //限制docker容器使用资源
        hostConfig.withMemory(memoryLimit);
        hostConfig.withMemorySwap(memorySwapLimit);
        hostConfig.withCpuCount(cpuLimit);
        hostConfig.withNetworkMode("none");  //禁用网络
        hostConfig.withReadonlyRootfs(true); //禁止在root目录写文件
        return hostConfig;
    }

    private String createContainerDir(String containerName) {
        String codeDir = System.getProperty("user.dir") + File.separator + JudgeConstants.CODE_DIR_POOL;
        if (!FileUtil.exist(codeDir)) {
            FileUtil.mkdir(codeDir);
        }
        return codeDir + File.separator + containerName;
    }

    public String getCodeDir(String containerId) {
        String containerName = containerNameMap.get(containerId);
        log.info("containerName: {}", containerName);
        return System.getProperty("user.dir") + File.separator + JudgeConstants.CODE_DIR_POOL
                + File.separator + containerName;
    }

}
