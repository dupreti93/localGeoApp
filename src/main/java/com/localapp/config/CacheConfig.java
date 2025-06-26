package com.localapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("apiKeys", "mapboxTiles", "placeSuggestions");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterAccess(24, TimeUnit.HOURS)
                .maximumSize(1000));
        return cacheManager;
    }
}