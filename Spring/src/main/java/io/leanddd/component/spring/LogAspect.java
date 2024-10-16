package io.leanddd.component.spring;

import com.alibaba.fastjson.JSON;
import io.leanddd.component.common.Util;
import io.leanddd.component.framework.Context;
import io.leanddd.component.logging.api.OperateLog;
import io.leanddd.component.logging.api.OperateLogService;
import io.leanddd.component.meta.Command.LogType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

@Aspect
@Order(-1)
@Slf4j
@Component
public class LogAspect implements BaseAspect {

    public static final String ANSI_RESET = "\033[0m";
    public static final String ANSI_RED = "\033[31m";

    private static final Logger auditLog = LoggerFactory.getLogger("AuditLogger");

    @Autowired
    ServiceAspect serviceAspect;

    /**
     * to write to audit log file to import to ELK
     */
    @Value("${app.component-features.audit-log:false}")
    boolean isAuditEnabled = false;

    @Value("${app.log:}")
    String logMode;

    /**
     * to write the service log (with @Log) to operatelog table
     */
    @Value("${app.component-features.operate-log:false}")
    boolean isOperateLogEnabled = false; // log to db for custom method

    @Autowired(required = false)
    OperateLogService logService;

    @Around("io.leanddd.component.spring.ServiceAspect.TheServiceAspect()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        var context = serviceAspect.getContext(pjp);
        if (!context.needAspect() || (logService != null && context.theClass.isAssignableFrom(logService.getClass())))
            return pjp.proceed();

        var isRest = false;

        String clazzName = context.theClass.getSimpleName();
        boolean needOperateLog = false;
        boolean needLog = Objects.equals(this.logMode, "command-only") ? (context.aCommand != null ? true : false) : true;
        if (context.aCommand != null
                && (context.aCommand.log() == LogType.Yes || context.aCommand.log() == LogType.Default)) {
            needOperateLog = true;
        }

        long beginTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        String requestURI = null;
        String parameters = null;
        String requestMethod = null;
        String reqId = null;
        String userId = Context.getUserId();
        String userName = Context.getUsername();

        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra != null) {
            reqId = (String) sra.getAttribute("_requestId", ServletRequestAttributes.SCOPE_REQUEST);
            if (reqId == null) {
                reqId = UUID.randomUUID().toString().replace("-", "").toLowerCase().substring(20);
                sra.setAttribute("_requestId", reqId, ServletRequestAttributes.SCOPE_REQUEST);
            }
            sra.getResponse().setHeader("requestId", reqId);
            requestMethod = context.request.getMethod();

            // String remoteAddress = request.getRemoteAddr();
            requestURI = context.request.getRequestURI();
        }

        Operation aApi = context.method.getAnnotation(Operation.class);
        String[] parameterNames = context.methodSig.getParameterNames();
        if (parameterNames != null && parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals("bindingResult")) {
                    break;
                }
                if ((context.parameterValues[i] instanceof HttpServletRequest)
                        || (context.parameterValues[i] instanceof HttpServletResponse)) {
                    sb.append("[").append(parameterNames[i]).append("=").append(context.parameterValues[i]).append("]");
                } else {
                    sb.append("[").append(parameterNames[i]).append("=")
                            .append(Util.getString(context.parameterValues[i]))
                            .append("]");
                }
            }
        }
        parameters = sb.toString();

        var color = context.aCommand != null ? ANSI_RED : "";
        if (needLog)
            log.info("{}// {} URI：【{} {}】 Method：{}【{}.{}】 Params->：【{}】{}", color, reqId, requestMethod, requestURI,
                    isRest ? "-REST-" : "", clazzName, context.methodName, parameters, ANSI_RESET);

        Object result = null;
        int records = 0;
        Throwable throwable = null;
        try {
            result = pjp.proceed();

        } catch (Throwable _throwable) {
            throwable = _throwable;
            if (throwable instanceof UndeclaredThrowableException) {
                throwable = ((UndeclaredThrowableException) throwable).getUndeclaredThrowable();
            }
            log.error(
                    " *****   " + (context.request != null ? context.request.hashCode() : "") + "     ERROR: "
                            + " URI：【{}】 Method:【{}.{}】 Params->：【{}】\r\n error: {}",
                    requestURI, clazzName, context.methodName, sb.toString(), throwable.getMessage());
            throw throwable;
        } finally {

            if (Util.isEmpty(userId)) {
                userId = Context.getUserId();
                userName = Context.getUsername();
            }

            String resultString = "";
            if (result != null) {
                if (result instanceof Collection) {
                    records = ((Collection<?>) result).size();
                    String retType = result.getClass().getComponentType() == null ? null
                            : result.getClass().getComponentType().getSimpleName();
                    if (retType == null && records > 0) {
                        Object obj = ((Collection<?>) result).iterator().next();
                        retType = obj != null ? obj.getClass().getSimpleName() : null;
                    }
                    resultString = String.format("%s (%d records)", retType, records);
                } else {
                    resultString = result.getClass().getSimpleName();
                }
            }

            long endTime = System.currentTimeMillis();
            long usedTime = endTime - beginTime;

            if (needLog) {
                if (usedTime > 5000)
                    log.warn("{} \\\\ {} Duration：【{}】 URI：【{} {}】{} Method：【{}.{}】 Return->：【{}】{}", color, reqId, usedTime,
                            requestMethod, requestURI, isRest ? "-REST-" : "", clazzName, context.methodName, resultString, ANSI_RESET);
                else
                    log.info("{} \\\\ {} Duration：【{}】 URI：【{} {}】{} Method：【{}.{}】 Return->：【{}】{}", color, reqId, usedTime,
                            requestMethod, requestURI, isRest ? "-REST-" : "", clazzName, context.methodName, resultString, ANSI_RESET);
            }

            // Map<String, Object> params = ServiceAspect.threadLocal.get();
            if (isAuditEnabled) {
                Map<String, Object> logContent = new HashMap<String, Object>();
                logContent.put("userId", userId);
                logContent.put("userName", userName);
                logContent.put("isRest", "" + isRest);
                if (isRest) {
                    if (aApi != null)
                        logContent.put("apiDesc", aApi.summary());
                }
                logContent.put("timestamp", Util.datetimeFormatter.format(new Date(beginTime)));
                logContent.put("endTime", endTime);
                logContent.put("beginTime", beginTime);
                logContent.put("duration", usedTime);
                if (context.request != null) {
                    logContent.put("reqId", reqId);
                    logContent.put("reqReferer", context.request.getHeader("Referer"));
                }
                logContent.put("method", context.methodName);
                logContent.put("class", clazzName);
                logContent.put("reqURI", requestURI);
                logContent.put("reqMethod", requestMethod);
                logContent.put("parameters", parameters);
                logContent.put("result", resultString);
                logContent.put("records", records);
                logContent.put("state", throwable == null ? "Success" : "Failure");
                /*
                if (params != null)
                    logContent.putAll(params);
                 */
                if (throwable != null) {
                    String msg = throwable.getMessage();
                    String exceptionName = throwable.getClass().getSimpleName() + ":"
                            + ((msg == null) ? "null" : msg.substring(0, msg.length() > 20 ? 20 : msg.length()));
                    logContent.put("exceptionName", exceptionName);
                    logContent.put("exception", Util.getErrStack(throwable, 0, 0));
                }
                String content = JSON.toJSONString(logContent);
                var auditMarker = MarkerFactory.getMarker("Audit");
                auditLog.info(auditMarker, content);
            }

            if (isOperateLogEnabled && (needOperateLog || throwable != null) && logService != null) {
                OperateLog log = null;
                if (throwable == null) {
                    log = OperateLog.builder().timestamp(new Date(beginTime))
                            .duration((int) (System.currentTimeMillis() - beginTime)).resultMsg("INFO").content("-")
                            .userId(userId).username(userName).resourceId(Context.getResourceDescriptor()).success(true)
                            .build();
                } else {
                    log = OperateLog.builder().timestamp(new Date(beginTime))
                            .resourceId(Context.getResourceDescriptor())
                            .duration((int) (System.currentTimeMillis() - beginTime)).resultMsg("ERROR").success(false)
                            .content("-").userId(userId).username(userName).success(false).build();
                    var knownExceptions = List.of("BizException", "BadCredentialsException");
                    final var exception = throwable;
                    var detailException = !knownExceptions.stream()
                            .anyMatch(ex -> exception.getClass().getSimpleName().equals(ex));
                    log.setResultMsg(detailException ? Util.getErrStack(throwable, 3, 900) : throwable.getMessage());
                }
                // module
                Tag api = context.theClass.getAnnotation(Tag.class);
                String module = null;
                if (api != null) {
                    // get @API.name first
                    if (Util.isNotEmpty(api.name())) {
                        module = api.name();
                    }
                }
                if (Util.isEmpty(module)) {

                    module = Util.isEmpty(context.aService.name()) ? clazzName : context.aService.name();
                }
                log.setModule(module);
                log.setTraceId(reqId);

                String desc = context.aCommand != null ? context.aCommand.value() : null;
                if (Util.isEmpty(desc) && aApi != null)
                    desc = aApi.summary();
                if (Util.isEmpty(desc))
                    desc = context.methodName;
                log.setOperateName(desc);

                if (context.aCommand != null && context.aCommand.logParam())
                    log.setOperateParams(JSON.toJSONString(context.parameterValues));

                logService.persist(log);
            }
        }
        return result;
    }
}
