package org.example.sanjiang.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有API请求
                .excludePathPatterns(
                        "/api/auth/login",      // 管理员登录
                        "/api/auth/register",   // 管理员注册
                        "/api/users/login",     // 用户登录
                        "/api/users/register",  // 用户注册
                        "/api/users/verify-code" // 验证码
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到本地文件系统路径，让图片可以通过HTTP访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:E:/Claude Code/backend/src/main/resources/static/uploads/")
                .setCachePeriod(3600); // 缓存1小时
    }
}