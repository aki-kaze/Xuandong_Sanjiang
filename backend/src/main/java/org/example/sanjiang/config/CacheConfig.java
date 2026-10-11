package org.example.sanjiang.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Redis缓存配置由application.properties管理
    // 缓存策略:
    // - navConfigs: 导航配置缓存，1小时过期
    // - 数据变更时通过@CacheEvict自动清除
}
