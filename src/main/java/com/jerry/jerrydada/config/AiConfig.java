package com.jerry.jerrydada.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    /**
     * apiKey 从开放平台获取
     */
    private String apiKey;

    @Bean
    public ClientV4 getClientV4() {
        // 创建客户端
        return new ClientV4.Builder(apiKey).build();
    }
}
