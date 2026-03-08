package io.leanddd.component.framework;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Response<T> {
    int httpStatus;
    boolean success;
    String errCode;
    String message;
    Date timestamp;
    T result;
    String type;
    int totalCount;
    Pagination pagination;

    public Response(int httpStatus, boolean success, String errCode, String message, Date timestamp, T result, String type, int totalCount) {
        this.httpStatus = httpStatus;
        this.success = success;
        this.errCode = errCode;
        this.message = message;
        this.timestamp = timestamp;
        this.result = result;
        this.type = type;
        this.totalCount = totalCount;
    }

    public Response(int httpStatus, boolean success, String errCode, String message, Date timestamp, T result, String type, int totalCount, Pagination pagination) {
        this(httpStatus, success, errCode, message, timestamp, result, type, totalCount);
        this.pagination = pagination;
    }
}
