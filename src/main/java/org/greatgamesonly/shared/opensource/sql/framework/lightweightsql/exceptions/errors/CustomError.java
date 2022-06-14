package org.greatgamesonly.shared.opensource.sql.framework.lightweightsql.exceptions.errors;

public abstract class CustomError {
    private String errorCode;
    private String reason;
    private String appendToReason;
    private int httpStatusCode;

    public CustomError(String errorCode, String reason, int httpStatusCode) {
        this.errorCode = errorCode;
        this.reason = reason;
        this.httpStatusCode = httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getReason() {
        if(appendToReason != null) {
            return reason + ", " + appendToReason;
        } else {
            return reason;
        }
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void appendToReason(String toAppend) {
        appendToReason=toAppend;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
