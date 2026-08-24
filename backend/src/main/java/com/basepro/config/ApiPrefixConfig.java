package com.basepro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 管理端接口统一加 /admin-api，用户端接口统一加 /app-api，避免改每个 Controller 的映射。
 */
@Configuration
public class ApiPrefixConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/app-api", HandlerTypePredicate.forBasePackage("com.basepro.app"));
        configurer.addPathPrefix("/admin-api", HandlerTypePredicate.forAnnotation(RestController.class)
                .and(clazz -> !clazz.getPackageName().startsWith("com.basepro.app")));
    }

}
