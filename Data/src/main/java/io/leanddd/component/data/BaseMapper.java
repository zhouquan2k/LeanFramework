package io.leanddd.component.data;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

public interface BaseMapper<T> {

    @SelectProvider(type = QueryProvider.class, method = "queryByExample")
    @ResultMap("Example")
    <P> List<P> queryByExample(@Param("entityClass") Class<P> entityClass, @Param("example") Map<String, Object> example,
                               @Param("select") String select, @Param("fixedParams") Map<String, Object> fixedParams);

    @SelectProvider(type = QueryProvider.class, method = "queryByExample")
    @ResultMap("Example")
    <P> List<P> queryByExampleWithCustomConditions(@Param("entityClass") Class<P> entityClass, @Param("example") Map<String, Object> example,
                                                   @Param("select") String select, @Param("fixedParams") Map<String, Object> fixedParams,
                                                   @Param("customConditions") List<String> customConditions);

    // for use with initData
    @SelectProvider(type = QueryProvider.class, method = "initRow")
    void initRow(@Param("data") Object data);

    List<T> queryAll();

    T getById(String id);
}
