package edu.hust.interceptor;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.hust.utils.JwtUtils;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        // 获取token
        @Nullable String token = request.getHeader("Authorization");
        try {
            // 如果验证成功放行请求
            DecodedJWT verify = JwtUtils.verify(token);
            request.setAttribute("userId", verify.getClaim("userId").asString());
            return true;
        } catch (Exception exception) {
            map.put("code", "401");
            map.put("msg", "token 已失效，请重新登录");
        }
        String json = new ObjectMapper().writeValueAsString(map);
        response.setContentType("application/json:charset=UTF=8");
        response.getWriter().println(json);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}