package io.leanddd.component.test;

import io.leanddd.component.common.Util;
import io.leanddd.component.data.EntityHelper;
import io.leanddd.component.framework.Context;
import io.leanddd.component.meta.Meta;
import io.leanddd.component.meta.MetaEntity;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.leanddd.component.meta.Meta.BooleanEx.False;

public class TestUtil {

    public static MockedStatic<Context> contextMock; // = Mockito.mockStatic(Context.class);

    public static MockedStatic<EntityHelper> entityHelperMock; // = Mockito.mockStatic(EntityHelper.class);

    private final static String NULL = "<NULL>";

    public static void init() {
        contextMock = Mockito.mockStatic(Context.class);
        entityHelperMock = Mockito.mockStatic(EntityHelper.class);
    }

    public static void tearDown() {
        entityHelperMock.close();
        contextMock.close();
    }


    @SuppressWarnings("rawtypes")
    private static boolean isComposite(Object obj) {
        if (obj == null) return false;
        // for object/entity
        return obj instanceof Map && !((Map) obj).isEmpty() || obj.getClass().getAnnotation(MetaEntity.class) != null;
    }

    @SuppressWarnings("rawtypes")
    private static int isArray(Object obj) {
        if (obj instanceof List) {
            return ((List) obj).size();
        }
        return -1;
    }

    final static Set<String> ignoreProperties = Set.of("createdTime", "updatedTime", "del", "delFlag", "class", "new", "version");

    @SuppressWarnings("rawtypes")
    private static Object getArrayItem(Object obj, int index) {
        return ((List) obj).get(index);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Set<String> getPropertyNames(Object obj) {
        if (obj instanceof Map) {
            var map = (Map) obj;
            return (Set<String>) map.keySet();
        }
        if (obj.getClass().getAnnotation(MetaEntity.class) != null) {
            var entityClass = obj.getClass();
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(entityClass);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                return Util.toSet(Arrays.stream(propertyDescriptors).map(property -> property.getName())
                        .filter(propertyName -> !ignoreProperties.contains(propertyName)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object getProperty(Object obj, String property) {
        if (obj instanceof Map) {
            var map = (Map) obj;
            return map.getOrDefault(property, NULL);
        }
        if (obj.getClass().getAnnotation(MetaEntity.class) != null) {
            var entityClass = obj.getClass();
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(entityClass);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (propertyDescriptor.getName().equals(property)) {
                        return propertyDescriptor.getReadMethod().invoke(obj);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    public static Object simulatePersist(Object entity) {
        Class<?> entityClass = entity.getClass();
        try {
            BeanInfo beanInfo = null;
            beanInfo = Introspector.getBeanInfo(entityClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                var propertyName = propertyDescriptor.getName();
                if (ignoreProperties.contains(propertyName))
                    continue;
                try {
                    var field = entityClass.getDeclaredField(propertyName);
                    var meta = field.getAnnotation(Meta.class);
                    if (meta.persistable() == False) {
                        propertyDescriptor.getWriteMethod().invoke(entity, new Object[]{null});
                    }
                    if (meta.value() == Meta.Type.ToMany) {
                        List<?> list = (List<?>) propertyDescriptor.getReadMethod().invoke(entity);
                        list.stream().map(item -> simulatePersist(item)).collect(Collectors.toList());
                        propertyDescriptor.getWriteMethod().invoke(entity, list);
                    }
                } catch (NoSuchFieldException ee) {
                    System.out.println("Field not found: " + propertyName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    public static boolean compareObjects(Object expected, Object result) {
        var match = true;
        match = match && compareObjects("", expected, result);
        System.out.println("----------------- Expeceted ^ Actual V");
        match = match && compareObjects("", result, expected);
        System.out.println("====================");
        return match;
    }

    private static boolean compareObjects(String path, Object expected, Object result) {
        if (expected == null && result == null) {
            System.out.println(String.format("%s%s: %s == %s", "   ", path, expected, result));
            return true;
        } else if (expected == null || result == null) {
            System.out.println(String.format("%s%s:  %s == %s ", "***", path, expected, result));
            return false;
        }
        var arraySize = isArray(expected);
        if (arraySize >= 0) {
            var targetSize = isArray(result);
            if (targetSize != arraySize) {
                System.out.println(String.format("%s%s :array(%d) == array(%d) %s", "***", path, arraySize, targetSize,
                        "array type.size not matched"));
                return false;
            }
            var compareResult = true;
            for (var i = 0; i < arraySize; i++) {
                var thisResult = compareObjects(String.format("%s/[%d]", path, i), getArrayItem(expected, i),
                        getArrayItem(result, i));
                compareResult = compareResult && thisResult;
            }
            return compareResult;
        } else if (isComposite(expected)) {
            var propertyNames = getPropertyNames(expected);
            var compareResult = true;
            for (var propertyName : propertyNames) {
                var thisResult = compareObjects(path + "/" + propertyName, getProperty(expected, propertyName),
                        getProperty(result, propertyName));
                var result2 = true;
                // TODO
                compareResult = compareResult && thisResult && result2;

            }
            return compareResult;
        } else {
            var compareResult = expected.getClass() == result.getClass() && expected.equals(result);
            System.out
                    .println(String.format("%s%s:  %s == %s", compareResult ? "   " : "***", path, expected, result));
            if (!compareResult) {
                System.out.print("");
            }
            return compareResult;
        }

    }

}