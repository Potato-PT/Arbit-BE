package com.arbit.app.common.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.detailMessage = errorCode.message();
    }

    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return detailMessage;
    }
}
