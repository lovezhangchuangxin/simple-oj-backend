package edu.hust.sandbox;

import lombok.Data;

/**
 * 代码执行结果
 */
@Data
public class CodeExecuteResult {
    private Byte status;
    private String output;
    private String error;
    private Short time;
    private Boolean timeout;
    private Short memory;
}
