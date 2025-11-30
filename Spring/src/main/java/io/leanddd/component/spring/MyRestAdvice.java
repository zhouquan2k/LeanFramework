package io.leanddd.component.spring;

import io.leanddd.component.framework.Context;
import io.leanddd.component.framework.NoCommonResponse;
import io.leanddd.component.framework.PaginationList;
import io.leanddd.component.framework.Response;
import io.leanddd.component.meta.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@ControllerAdvice(annotations = {Service.class})
@ConditionalOnProperty(name = "app.component-features.web", havingValue = "true", matchIfMissing = false) // 允许使用
@Slf4j
public class MyRestAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        Context.resetThreadLocal();
        var l = request.getHeaders().get("_NEED_TIMESTAMP");
        var needTimestamp = !(l != null && l.size() > 0 && Objects.equals(l.get(0), "false"));
        var date = needTimestamp ? new Date() : null;

        if (body instanceof Optional) {
            body = ((Optional<?>) body).orElseThrow();
        }
        if (body instanceof List) {
            List<?> list = (List<?>) body;
            var size = list.size();
            if (size > 1000) {
                log.warn("return too many rows: " + request.getURI());
                list = list.subList(0, 1000);
            }
            var type = !list.isEmpty() ? list.get(0).getClass().getSimpleName() : null;
            if (body instanceof PaginationList<?>) {
                PaginationList<?> plist = (PaginationList<?>) body;
                size = plist.getPagination().getTotalCount();
            }
            return new Response(HttpStatus.OK.value(), true, null, null, date, list, type, size);
        }
        else if (body instanceof NoCommonResponse)
            return body;
        else if (body instanceof BufferedImage) {
            return body;
        } else if (selectedContentType != null && Stream.of(MediaType.TEXT_HTML, MediaType.TEXT_MARKDOWN).anyMatch(selectedContentType::includes)) {
            return body;
        }
        var type = body != null ? body.getClass().getSimpleName() : null;
        return new Response(HttpStatus.OK.value(), true, null, null, date, body, type, body == null ? 0 : 1);
    }
}
