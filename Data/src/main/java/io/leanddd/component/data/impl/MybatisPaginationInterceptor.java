package io.leanddd.component.data.impl;

import io.leanddd.component.framework.Pagination;
import io.leanddd.component.framework.PaginationList;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Intercepts({
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
})
public class MybatisPaginationInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        // RowBounds rowBounds = (RowBounds) invocation.getArgs()[2];
        Pagination pagination = getPagination(parameter);
        // 判断是否分页（offset/limit 不为默认）
        if (pagination == null || pagination.getLimit() == null || pagination.getLimit() <= 0 ) {
            return invocation.proceed();
        }
        Executor executor = (Executor) invocation.getTarget();

        // 获取原 SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        if (pagination.getTotalCount() == null) {
            // 自动生成 count SQL
            String countSql = buildCountSql(originalSql);
            // 执行 count 查询
            int total = (int)executeCount(ms, parameter instanceof Pagination ? null : parameter, boundSql, countSql, executor);
            pagination.setTotalCount((int) total);
        }

        String pageSql = originalSql + " LIMIT " + pagination.getOffset() + ", " + pagination.getLimit();
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), pageSql,
                boundSql.getParameterMappings(), parameter);

        MappedStatement newMs = copyMappedStatement(ms, newBoundSql);
        invocation.getArgs()[0] = newMs;
        invocation.getArgs()[2] = RowBounds.DEFAULT;

        List<?> result = (List<?>) invocation.proceed();

        // 封装分页结果
        return new PaginationList(result, pagination);
    }

    private Pagination getPagination(Object parameter) {
        if (parameter instanceof Pagination) {
            return (Pagination) parameter;
        }
        if (parameter instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) parameter;
            for (Object value : map.values()) {
                if (value instanceof Pagination) {
                    return (Pagination) value;
                }
            }
        }// 根据实际情况调整获取 Pagination 的方式
        return null;
    }

    private long executeCount(MappedStatement ms, Object parameter, BoundSql boundSql, String countSql, Executor executor) throws Exception {
        BoundSql countBoundSql = new BoundSql(ms.getConfiguration(), countSql,
                boundSql.getParameterMappings(), parameter);

        String countMsId = ms.getId() + "!count";
        List<ResultMap> countResultMaps = Collections.singletonList(
                new ResultMap.Builder(ms.getConfiguration(), countMsId + "-inline", Long.class, Collections.emptyList()).build()
        );
        MappedStatement countMs = copyMappedStatement(ms, countBoundSql, countResultMaps, countMsId);

        CacheKey cacheKey = executor.createCacheKey(countMs, parameter, RowBounds.DEFAULT, countBoundSql);
        List<Object> countList = executor.query(countMs, parameter, RowBounds.DEFAULT, null, cacheKey, countBoundSql);
        if (countList == null || countList.isEmpty()) {
            return 0L;
        }
        return ((Number) countList.get(0)).longValue();
    }

    private String buildCountSql(String sql) {
        sql = sql.replaceAll("(?i)ORDER BY[\\s\\S]*", "");
        int idx = sql.toLowerCase().indexOf("from");
        return "SELECT COUNT(*) " + sql.substring(idx);
    }

    private MappedStatement copyMappedStatement(MappedStatement ms, BoundSql newBoundSql) {
        return copyMappedStatement(ms, newBoundSql, ms.getResultMaps(), ms.getId());
    }

    private MappedStatement copyMappedStatement(MappedStatement ms, BoundSql newBoundSql, List<ResultMap> resultMaps, String newId) {
        String msId = newId != null ? newId : ms.getId();
        return new MappedStatement.Builder(ms.getConfiguration(), msId, p -> newBoundSql,
                ms.getSqlCommandType())
                .resultMaps(resultMaps)
                .build();
    }
}
