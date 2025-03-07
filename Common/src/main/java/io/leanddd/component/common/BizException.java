package io.leanddd.component.common;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    String errCode = "Generic";

    String messagePattern;
    Object[] messageParams;
    String errDetail; // show to developer

    int httpStatus;

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, String errCode, String errDetail) {
        super(message);
        this.errCode = errCode;
        this.errDetail = errDetail;
    }

    public BizException(String messagePattern, Object[] messageParams, String errCode, String errDetail) {
        super(String.format(messagePattern, messageParams));
        this.messagePattern = messagePattern;
        this.messageParams = messageParams;
        this.errCode = errCode;
        this.errDetail = errDetail;
    }

    public BizException(String errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    public BizException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
