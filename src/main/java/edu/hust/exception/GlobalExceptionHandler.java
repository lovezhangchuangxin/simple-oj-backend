package edu.hust.exception;

import edu.hust.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("发生异常", e);
        return Result.fail(1000, "发生异常");
    }

    @ExceptionHandler(HustOjException.class)
    public Result handleHustException(HustOjException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }
}
