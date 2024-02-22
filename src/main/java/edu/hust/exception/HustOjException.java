package edu.hust.exception;

public class HustOjException extends RuntimeException {
    private final ExceptionCodeEnum code;

    public HustOjException(ExceptionCodeEnum code) {
        this(code, code.getMsg());
    }

    public HustOjException(ExceptionCodeEnum code, String message) {
        super(message);
        this.code = code;
    }


    public Integer getCode() {
        return code.getCode();
    }
}
