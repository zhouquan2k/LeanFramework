package io.leanddd.component.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    public static final String GenericError = "Generic.Unspecified";
    public static final String BusinessRuleError = "BusinessRule.Violation";

    public static void notSupport() {
        throw new RuntimeException("NOT SUPPORTED!");
    }

    public static void check(boolean b) {
        if (!b)
            throw new RuntimeException("assert failure");
    }

    public static void check(boolean b, String msgFormat, Object... params) throws RuntimeException {
        if (!b)
            throw new RuntimeException(String.format(msgFormat, params));
    }

    public static void checkBiz(boolean b, String errCode, String msgFormat, Object... params) throws BizException {
        if (!b)
            throw new BizException(errCode, String.format(msgFormat, params));
    }

    // for string
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    // date
    public static final String dateFormatterString = "yyyy-MM-dd";
    public static final String datetimeFormatterString = "yyyy-MM-dd HH:mm:ss";
    public static SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormatterString);
    public static SimpleDateFormat datetimeFormatter = new SimpleDateFormat(datetimeFormatterString);

    public static void initDateFormat(String timezone) {
        dateFormatter.setTimeZone(TimeZone.getTimeZone(timezone));
        datetimeFormatter.setTimeZone(TimeZone.getTimeZone(timezone));
    }

    public static Date getDate(String dateStr) {
        try {
            return dateFormatter.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    // collections
    public static <T> Map<String, T> initMap(Object[][] pairs) {
        Map<String, T> ret = new HashMap<String, T>();
        for (int i = 0; i < pairs.length; i++)
            ret.put((String) pairs[i][0], (T) pairs[i][1]);
        return ret;
    }

    public static Object getOrDefaultEntry(Map<String, Object> map, String key, Object defaultValue) {
        return map.containsKey(key) ? (map.get(key) == null ? defaultValue : map.get(key)) : defaultValue;
    }

    public static <I, O> List<O> mapToList(Stream<I> stream, Function<I, O> func) {
        return stream.map(func).collect(Collectors.toList());
    }

    public static <T> List<T> toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

    public static <T> Set<T> toSet(Stream<T> stream) {
        return stream.collect(Collectors.toSet());
    }

    public static <T, K> Map<K, T> toMap(Stream<T> from, Function<T, K> keyFunc) {
        return from.collect(Collectors.toMap(keyFunc, Function.identity()));
    }

    public static <T, K, V> Map<K, V> toMap(Stream<T> from, Function<T, K> keyFunc, Function<T, V> valueFunc) {
        return from.collect(Collectors.toMap(keyFunc, valueFunc));
    }

    // string representation
    public static String getString(Object o) {
        return (o == null) ? "<null>" : o.toString();
    }

    public static String getErrStack(Throwable exception, int maxLine, int maxLength)
    {
        if (maxLine < 0) {
            ByteArrayOutputStream stream0 = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(stream0);
            exception.printStackTrace(stream);
            String ret = stream0.toString();
            return (maxLength <= 0) ? ret : ((ret.length() > maxLength) ? ret.substring(0, maxLength) : ret);
        } else {
            Throwable cause = exception.getCause();
            if (exception instanceof InvocationTargetException)
                return getErrStack(((InvocationTargetException) exception).getTargetException(), maxLine, maxLength);

            String ret = "";
            ret += String.format("[%s] %s\r\n", exception.getClass().getName(), exception.getMessage());

            StackTraceElement[] elements = exception.getStackTrace();
            int line = 0;
            for (StackTraceElement e : elements) {
                ret += "\tat " + e.toString() + "\r\n";
                line++;
                if (maxLine > 0 && line > maxLine)
                    break;
            }

            if (cause != null)
                ret += String.format("Caused by: ") + getErrStack(cause, maxLine, -1);
            return (maxLength <= 0) ? ret : ((ret.length() > maxLength) ? ret.substring(0, maxLength) : ret);
        }
    }

    // pattern format
    static public Pattern paramPattern = Pattern.compile("\\$\\{([^\\}]+)\\}");

    public static String format(String template, Map<String, ?> data) {
        Matcher m = paramPattern.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String property = m.group(1);
            Object prop = data.get(property);
            m.appendReplacement(sb, prop == null ? "<null>" : prop.toString());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // reflection
    public static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field f = obj.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(obj);
    }

    public static Object getBeanProperty(Object bean, String propertyName) {
        try {
            Class<?> beanClass = bean.getClass();
            String methodName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            Method getter = beanClass.getMethod(methodName);
            return getter.invoke(bean);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(Class<?> cls, String methodName, Object[] params) {
        try {
            Method[] methods = cls.getMethods();
            for (Method method : methods) {
                String name = method.getName();
                int paramCnt = method.getParameterTypes().length;
                if (methodName.equals(name) && (params == null || paramCnt == params.length))
                    return method;
            }
            if (params != null) { // multi names or no names,use param types to guess   
                Class<?>[] paramTypes = new Class[params.length];
                for (int i = 0; i < params.length; i++)
                    paramTypes[i] = (params[i] == null) ? null : params[i].getClass();
                return cls.getMethod(methodName, paramTypes);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
