package com.basepro.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.basepro.tenant.TenantLineHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：只装官方插件，不做二次封装。
 * <p>
 * Mapper 直接继承 {@link com.baomidou.mybatisplus.core.mapper.BaseMapper}，
 * 复杂查询用 LambdaQueryWrapper 或 XML，需要跳过租户拦截时用官方注解
 * {@link com.baomidou.mybatisplus.annotation.InterceptorIgnore}。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(AppProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件必须放在分页插件之前
        if (properties.tenant().enabled()) {
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(
                    new TenantLineHandlerImpl(properties.tenant().ignoreTables())));
        }
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 拦截没有 where 条件的 update / delete
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

}
