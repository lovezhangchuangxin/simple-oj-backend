package edu.hust.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import edu.hust.service.VerificationCodeService;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final LoadingCache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(60 * 3, TimeUnit.SECONDS) // 设置验证码有效期为60秒
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(@Nullable String key) throws Exception {
                    return generateRandomCode(); // 自定义生成随机验证码的方法
                }
            });

    /**
     * 发送验证码
     */
    public void sendVerificationCodeToUser(String key) {
        String code = getOrGenerateCodeForUser(key);

        // 发送验证码到用户邮箱或其他地址
        System.out.println("Sending verification code to " + key + ": " + code);
    }

    public boolean validateCode(String key, String inputCode) {
        try {
            String storedCode = cache.getIfPresent(key);
            if (inputCode != null && inputCode.equalsIgnoreCase(storedCode)) {
                // 验证码正确后从缓存中移除
                cache.invalidate(key);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getOrGenerateCodeForUser(String key) {
        try {
            return cache.get(key);
        } catch (ExecutionException | UncheckedExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve or generate verification code for user: " + key);
        }
    }

    /**
     * 生成随机验证码
     */
    private static String generateRandomCode() {
        return String.format("%06d", new Random().nextInt(100000, 999999));
    }

    public boolean isCodeExisted(String key) {
        return cache.getIfPresent(key) != null;
    }
}