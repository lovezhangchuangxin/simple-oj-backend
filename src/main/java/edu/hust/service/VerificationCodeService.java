package edu.hust.service;

public interface VerificationCodeService {
    /**
     * 生成验证码
     *
     * @param key 验证码的 key
     */
    String getOrGenerateCodeForUser(String key);

    /**
     * 校验验证码
     *
     * @param key       验证码的 key
     * @param inputCode 用户输入的验证码
     */
    boolean validateCode(String key, String inputCode);

    /**
     * 判断验证码是否存在
     *
     * @param key 验证码的 key
     */
    boolean isCodeExisted(String key);
}
