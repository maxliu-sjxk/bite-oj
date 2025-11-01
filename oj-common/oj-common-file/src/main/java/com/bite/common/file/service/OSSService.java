package com.bite.common.file.service;

import com.aliyun.oss.OSSClient;
import com.bite.common.file.config.OSSProperties;
import com.bite.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RefreshScope
public class OSSService {

    @Autowired
    private OSSProperties prop;

    @Autowired
    private OSSClient ossClient;

    @Autowired
    private RedisService redisService;

    @Value("${file.max-time}")
    private int maxTime;

    @Value("${file.test}")
    private boolean test;

//    public
}
