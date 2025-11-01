package com.bite.common.file.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OSSConfig {

    @Autowired
    private OSSProperties prop;

    public OSS ossClient;

    @Bean
    public OSS ossClient() {
        DefaultCredentialProvider credentialProvider = CredentialsProviderFactory.
                newDefaultCredentialProvider(prop.getAccessKeyId(), prop.getAccessKeySecret());

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 显式声明使用V4签名算法
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        ossClient = OSSClientBuilder.create()
                .credentialsProvider(credentialProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(prop.getRegion())
                .endpoint(prop.getEndpoint())
                .build();
        return ossClient;
    }

    @PreDestroy
    public void closeOSSClient() {
        ossClient.shutdown();
    }

}
