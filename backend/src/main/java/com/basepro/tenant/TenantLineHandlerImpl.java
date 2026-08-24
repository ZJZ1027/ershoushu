package com.basepro.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus 官方多租户插件的处理器：为 SQL 自动追加 tenant_id 条件。
 */
public class TenantLineHandlerImpl implements TenantLineHandler {

    private final Set<String> ignoreTables;

    public TenantLineHandlerImpl(Set<String> ignoreTables) {
        this.ignoreTables = ignoreTables == null ? Set.of()
                : ignoreTables.stream().map(table -> table.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
    }

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContext.getTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return TenantContext.isIgnored() || ignoreTables.contains(tableName.toLowerCase(Locale.ROOT));
    }

}
