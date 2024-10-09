package io.leanddd.component.framework;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
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
}