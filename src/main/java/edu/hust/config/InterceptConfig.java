package edu.hust.config;


import edu.hust.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptConfig implements WebMvcConfigurer {
    private final JwtInterceptor jwtInterceptor;

    public InterceptConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器
        registry.addInterceptor(jwtInterceptor)
                // 拦截的路径 需要进行token验证的路径
                .addPathPatterns("/**")
                // 放行的路径
                .excludePathPatterns("/user/register/*", "/user/login", "/user/verification", "/user/test");
    }
}