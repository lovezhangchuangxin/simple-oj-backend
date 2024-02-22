package edu.hust.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class JwtUtils {
    /**
     * 密钥要自己保管好
     */
    private static final String secret = "hustojzcx";
    /**
     * 过期时间
     */
    private static final Long expire = 7 * 24 * 3600 * 1000L;

    /**
     * 传入 payload 生成 token
     *
     * @return token
     */
    public static String genToken(@Nullable Map<String, String> map) {
        JWTCreator.Builder builder = JWT.create();
        if (map != null) {
            map.forEach(builder::withClaim);
        }
        builder.withExpiresAt(new Date(System.currentTimeMillis() + expire));
        return builder.sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证 token 合法性
     */
    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
    }

    /**
     * 获取 token 信息方法
     */
    public static Map<String, Claim> getClaims(String token) {
        return JWT.require(Algorithm.HMAC256(secret)).build().verify(token).getClaims();
    }

    /**
     * 解析请求头中的 token，获取负荷信息
     */
    public static Map<String, Claim> getClaims() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String token = request.getHeader("Authorization");
        return getClaims(token);
    }
}
