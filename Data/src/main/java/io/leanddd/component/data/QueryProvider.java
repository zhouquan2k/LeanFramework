package io.leanddd.component.data;

import java.util.Map;

// Helper of mybatis to build sql
public class QueryProvider {

    public static String queryByExample(Map<String, Object> params) {
        Object example = params.get("example");
        String sql = (String) params.get("select");
        Class<?> entityClass = (Class<?>) params.get("entityClass");
        var fixedParams = (Map<String, Object>) params.get("fixedParams");
        EntityHelper<?> queryHelper = EntityHelper.getInstance(entityClass);
        return queryHelper.queryByExample(example, sql, fixedParams);
    }

    public static String initRow(Map<String, Object> params) {
        var paramName = "param1";
        Object param = params.get(paramName);
        EntityHelper<?> queryHelper = EntityHelper.getInstance(param.getClass());
        return queryHelper.insert(param, paramName);
    }
}

