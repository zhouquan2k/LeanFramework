package io.leanddd.component.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

//TODO separate implementation
public class JsonHelper {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDateFormat(Util.dateFormatter);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJson(Object src) {
        try {
            return objectMapper.writeValueAsString(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String src, TypeReference<T> type) {
        try {
            if (Util.isEmpty(src)) {
                if (List.class.isAssignableFrom(type.getType().getClass())) {
                    return (T) List.of();
                } else
                    return null;
            } else
                return objectMapper.readValue(src, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
