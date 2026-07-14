package com.projects.task_manager.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class BucketConfig {

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedisClient redisClient() {
        // Automatically injects the password from application.properties if you have one set
        String uri = redisPassword.isEmpty() ?
                "redis://localhost:6379" :
                "redis://:" + redisPassword + "@localhost:6379";
        return RedisClient.create(uri);
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        return LettuceBasedProxyManager.builderFor(redisClient)
                // Ensures buckets automatically delete themselves from Redis after 15 minutes of inactivity
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(15)))
                .build();
    }
}