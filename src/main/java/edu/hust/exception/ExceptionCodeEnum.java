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

    UPLOAD_AVATAR_ERROR(1040, "上传头像失败"),

    PROBLEM_CREATE_ERROR(1050, "题目创建失败"),
    TIME_OR_MEMORY_ERROR(1051, "题目内存或时间参数错误"),
    PROBLEM_NOT_EXIST(1052, "题目不存在或不属于自己"),
    PROBLEM_DELETE_ERROR(1053, "题目删除失败"),
    PROBLEM_UPDATE_ERROR(1054, "题目更新失败"),
    PROBLEM_READ_ERROR(1055, "题目读取失败"),
    LANGUAGE_NOT_EXIST(1056, "暂不支持该语言"),
    USER_CODE_SAVE_ERROR(1057, "用户代码保存失败"),
    USER_CODE_DELETE_ERROR(1058, "用户代码删除失败"),

    BULLETIN_NOT_FOUND(1070, "公告不存在"),
    BULLETIN_ADD_ERROR(1071, "公告添加失败"),
    BULLETIN_DELETE_ERROR(1072, "公告删除失败"),
    BULLETIN_UPDATE_ERROR(1073, "公告更新失败"),

    COMPILE_ERROR(1080, "编译错误"),
    EXECUTE_ERROR(1081, "执行错误"),

    PERMISSION_DENIED(1090, "权限不足"),

    PROBLEM_NOTE_WRITE_ERROR(1100, "题解写入失败"),
    PROBLEM_NOTE_READ_ERROR(1101, "题解读取失败"),
    PROBLEM_NOTE_DELETE_ERROR(1102, "题解删除失败"),
    PROBLEM_NOTE_EXIST(1103, "题解已存在"),
    PROBLEM_NOTE_NOT_EXIST(1104, "题解不存在"),

    CLASS_CREATE_ERROR(1110, "班级创建失败"),
    CLASS_DELETE_ERROR(1111, "班级删除失败"),
    CLASS_UPDATE_ERROR(1112, "班级更新失败"),
    CLASS_NOT_EXIST(1113, "班级不存在"),
    CLASS_NAME_ERROR(1114, "班级名词不存在或者过长"),

    CLASS_USER_ADD_ERROR(1120, "添加班级用户失败"),
    CLASS_USER_DELETE_ERROR(1121, "删除班级用户失败"),
    CLASS_USER_EXIST(1122, "班级用户已存在"),
    CLASS_USER_NOT_EXIST(1123, "班级用户不存在");

    private final Integer code;
    private final String msg;

    ExceptionCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
