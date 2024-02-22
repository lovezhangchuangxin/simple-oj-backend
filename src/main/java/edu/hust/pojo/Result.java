package edu.hust.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private int code;
    private String msg;
    private Object data;

    public static Result success() {
        return new Result(0, "success", null);
    }

    public static Result success(Object data) {
        return new Result(0, "success", data);
    }

    public static Result success(String msg, Object data) {
        return new Result(0, msg, data);
    }

    public static Result fail(Integer code, String msg) {
        return new Result(code, msg, null);
    }

    public static Result fail(Integer code) {
        return new Result(code, "", null);
    }
}
