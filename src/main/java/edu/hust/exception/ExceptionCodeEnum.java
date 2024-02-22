package edu.hust.exception;

import lombok.Getter;

@Getter
public enum ExceptionCodeEnum {
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USERNAME_EXISTED(1003, "用户名已存在"),

    EMAIL_EXISTED(1010, "邮箱已存在"),
    EMAIL_NOT_FOUND(1011, "邮箱不存在"),
    EMAIL_FORMAT_ERROR(1012, "邮箱格式错误"),

    PARAMS_NULL(1020, "参数不能为空"),
    PARAMS_ERROR(1021, "参数错误"),

    VERIFICATION_CODE_ERROR(1030, "验证码错误"),
    VERIFICATION_CODE_EXISTED(1031, "验证码已存在"),

    UPLOAD_AVATAR_ERROR(1040, "上传头像失败");


    private final Integer code;
    private final String msg;

    ExceptionCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
