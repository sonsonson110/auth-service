package com.example.auth_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {
    @Bean
    @ConditionalOnProperty(name = "app.cache.provider", havingValue = "caffeine", matchIfMissing = true)
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache("forgotPasswordToken",
                Caffeine.newBuilder()
                        .initialCapacity(100)
                        .expireAfterWrite(15, TimeUnit.MINUTES)
                        .evictionListener((key, value, cause) ->
                                System.out.println("Cache entry evicted: " + key + ", cause: " + cause)
                        )
                        .removalListener((key, value, cause) ->
                                System.out.println("Cache entry removed: " + key + ", cause: " + cause)
                        )
                        .build());
        return cacheManager;
    }
}
