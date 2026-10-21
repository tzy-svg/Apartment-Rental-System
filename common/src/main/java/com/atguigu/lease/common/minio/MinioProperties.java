package com.atguigu.lease.common.minio;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;



}
