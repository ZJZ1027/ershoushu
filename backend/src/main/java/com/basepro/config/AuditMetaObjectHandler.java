package com.basepro.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.basepro.security.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计字段自动填充：创建/更新时间、创建人/更新人、逻辑删除标记。
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String username = SecurityUtils.getUsername();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "creator", String.class, username);
        strictInsertFill(metaObject, "updater", String.class, username);
        strictInsertFill(metaObject, "deleted", Boolean.class, Boolean.FALSE);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, "updater", String.class, SecurityUtils.getUsername());
    }

}
