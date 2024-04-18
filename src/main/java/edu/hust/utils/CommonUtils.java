package edu.hust.utils;

import jakarta.annotation.Nullable;

public class CommonUtils {
    /**
     * 验证邮箱格式
     */
    public static boolean isEmail(@Nullable String email) {
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        return email != null && email.matches(regex);
    }

    /**
     * 用户名格式校验，支持中文、字母、数字、下划线，4-16 位
     */
    public static boolean isUsername(@Nullable String username) {
        String regex = "^[\\u4e00-\\u9fa5_a-zA-Z0-9-]{2,16}$";
        return username != null && username.matches(regex);
    }

    /**
     * 密码格式校验，6-18 位，包含字母、数字、下划线
     */
    public static boolean isPassword(@Nullable String password) {
        String regex = "^[a-zA-Z0-9_-]{6,18}$";
        return password != null && password.matches(regex);
    }

    /**
     * 生成随机密码
     */
    public static String genRandomPassword() {
        StringBuilder password = new StringBuilder();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        for (int i = 0; i < 8; i++) {
            password.append(chars[(int) (Math.random() * chars.length)]);
        }
        return password.toString();
    }
}
