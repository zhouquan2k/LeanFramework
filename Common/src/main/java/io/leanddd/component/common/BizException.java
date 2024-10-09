package io.leanddd.component.common;

import lombok.Getter;

public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    @Getter
    String errCode = "Generic";
    // message show to end user
    @Getter
    String errDetail; // show to developer
    @Getter
    int httpStatus;

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, String errCode, String errDetail) {
        super(message);
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
