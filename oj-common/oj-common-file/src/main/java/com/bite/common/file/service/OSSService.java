package com.bite.common.file.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.ObjectId;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.utils.ThreadLocalUtils;
import com.bite.common.file.config.OSSProperties;
import com.bite.common.file.domain.OSSResult;
import com.bite.common.redis.service.RedisService;
import com.bite.common.security.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

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

    /**
     * 是否测试，为true表示测试，此时用户一天内上传文件次数无上限
     */
    @Value("${file.test}")
    private boolean test;

    /**
     * 上传文件
     * @param file
     * @return
     * @throws Exception
     */
    public OSSResult uploadFile(MultipartFile file) throws Exception {
        if (!test) {
            checkUploadCount();
        }
        InputStream inputStream = null;
        try {
            String fileName;
            if (file.getOriginalFilename() != null) {
                fileName = file.getOriginalFilename().toLowerCase();
            } else {
                fileName = "a.png";
            }
            String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
            inputStream = file.getInputStream();
            return upload(extName, inputStream);
        } catch (Exception e) {
            log.error("OSS upload file error", e);
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /**
     * 用户上传次数限制缓存设计：
     * 类型：hash
     * key: user:upload:times
     * field: userId
     * value: times
     *
     * 考虑：过期时间的设置是否有必要作为一个定时任务？
     * 即，能否接受 “当天每个用户首次上传时都会设置一遍hash key的过期时间”的性能消耗
     */
    private void checkUploadCount() {
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        //获取当前用户的上传次数
        Long times = redisService.getCacheMapValue(CacheConstants.USER_UPLOAD_TIMES_KEY, String.valueOf(userId), Long.class);
        //如果上传次数已经超过限制，则抛出异常
        if (times != null && times >= maxTime) {
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD_TIMES_LIMIT);
        }
        //走到这证明未达限制次数，上次上传次数+1
        redisService.incrementHashValue(CacheConstants.USER_UPLOAD_TIMES_KEY, String.valueOf(userId), 1);
        //为hash缓存设置过期时间，与验证码限制次数string类型的key不同，这里相当于共用一个过期时间，第二天过期
        //即所有用户第二天已上传次数都会重置为0
        //这里的逻辑是：当天每个用户首次上传时都会设置一遍该key的过期时间
        if (times == null || times == 0) {
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            redisService.expire(CacheConstants.USER_UPLOAD_TIMES_KEY, seconds, TimeUnit.SECONDS);
        }
    }

    private OSSResult upload(String fileType, InputStream inputStream) {
        // key pattern: file/id.xxx, cannot start with /
        String key = prop.getPathPrefix() + ObjectId.next() + "." + fileType;
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setObjectAcl(CannedAccessControlList.PublicRead);
        PutObjectRequest request = new PutObjectRequest(prop.getBucketName(), key, inputStream, objectMetadata);
        PutObjectResult putObjectResult;
        try {
            putObjectResult = ossClient.putObject(request);
        } catch (Exception e) {
            log.error("OSS put object error: {}", ExceptionUtil.stacktraceToOneLineString(e, 500));
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        }
        return assembleOSSResult(key, putObjectResult);
    }

    private OSSResult assembleOSSResult(String key, PutObjectResult putObjectResult) {
        OSSResult ossResult = new OSSResult();
        if (putObjectResult == null || StrUtil.isBlank(putObjectResult.getRequestId())) {
            ossResult.setSuccess(false);
        } else {
            ossResult.setSuccess(true);
            ossResult.setName(FileUtil.getName(key));
        }
        return ossResult;
    }
}
