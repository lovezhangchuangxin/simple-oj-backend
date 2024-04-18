package edu.hust;

import edu.hust.mapper.UserMapper;
import edu.hust.pojo.User;
import edu.hust.service.MailService;
import edu.hust.service.impl.VerificationCodeServiceImpl;
import edu.hust.utils.CommonUtils;
import edu.hust.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
class SimpleOjBackendApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailService mailService;
    @Autowired
    private VerificationCodeServiceImpl verificationCodeServiceImpl;

    @Test
    void contextLoads() {
    }

    @Test
    void testInsert() {
        User s = new User();
        s.setUsername("zcx123");
        s.setPassword("test123");
        s.setEmail("abc@qq.com");
        userMapper.insert(s);
        assert s.getId() != null;
    }

    @Test
    void testSendMail() {
        mailService.sendSimpleMail("2911331070@qq.com", "简单邮件测试", "这是一封SpringBoot普通测试邮件");
    }

    @Test
    void testGenJwt() {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", "zcx");
        String token = JwtUtils.genToken(map, false);
        System.out.println(token);
        JwtUtils.verify(token);
        JwtUtils.getClaims(token).forEach((k, v) -> System.out.println(k + ": " + v.asString()));
    }

    @Test
    void testEmail() {
        String email = "zcx";
        System.out.println(CommonUtils.isEmail(email));
    }

    @Test
    void testVerifyCode() {
        String code = verificationCodeServiceImpl.getOrGenerateCodeForUser("1");

        System.out.println(code);

        System.out.println(verificationCodeServiceImpl.validateCode("1", code));
    }
}
