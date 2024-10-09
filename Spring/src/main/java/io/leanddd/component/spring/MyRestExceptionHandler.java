package io.leanddd.component.spring;

import io.leanddd.component.common.BizException;
import io.leanddd.component.common.Util;
import io.leanddd.component.framework.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;

@ControllerAdvice
@ConditionalOnProperty(name = "app.component-features.web", havingValue = "true", matchIfMissing = false) // 允许使用
@Slf4j
public class MyRestExceptionHandler {

    private static Throwable getRootException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    @ExceptionHandler(value = Throwable.class)
    @ResponseBody
    public ResponseEntity<Response> AnyExceptionHandler(HttpServletRequest req, Throwable e) {
        return handleException(e);
    }

    private ResponseEntity<Response> handleException(Throwable e) { // ServerHttpResponse response,

        var logDetail = false;
        var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        var exceptionName = e.getClass().getSimpleName();
        ResponseEntity<Response> ret = null;
        if (e instanceof UndeclaredThrowableException) {
            e = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
        }
        var root = getRootException(e);
        var errMessage = String.format("【%s】 %s\n【RootCause】 %s\n", e.getClass().getSimpleName(), e.getMessage(), root.getMessage());
        if (e instanceof BizException) {
            BizException be = (BizException) e;
            ret = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(HttpStatus.BAD_REQUEST.value(), false,
                    be.getErrCode(), be.getMessage(), new Date(), null, null, 0));
            logDetail = false;
        } else {
            if (e instanceof AuthenticationException) {
                httpStatus = HttpStatus.FORBIDDEN;

            }
            if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
                logDetail = true;
            }

            ret = ResponseEntity.status(httpStatus)
                    .body(new Response(httpStatus.value(), false, httpStatus.getReasonPhrase() + "." + exceptionName,
                            errMessage,
                            new Date(), null, null, 0));
        }

        if (logDetail)
            log.error("***** " + Util.getErrStack(e, 0, 0));
        else
            log.error("***** " + errMessage);
        return ret;
    }
}
