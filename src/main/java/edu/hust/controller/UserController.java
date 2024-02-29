package edu.hust.controller;

import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.pojo.Result;
import edu.hust.pojo.User;
import edu.hust.service.MailService;
import edu.hust.service.UserService;
import edu.hust.service.impl.VerificationCodeServiceImpl;
import edu.hust.utils.AliOSSUtils;
import edu.hust.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final VerificationCodeServiceImpl verificationCodeServiceImpl;
    private final MailService mailService;

    public UserController(UserService userService, VerificationCodeServiceImpl verificationCodeServiceImpl, MailService mailService, AliOSSUtils aliOSSUtils) {
        this.userService = userService;
        this.verificationCodeServiceImpl = verificationCodeServiceImpl;
        this.mailService = mailService;
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/verification")
    public Result sendVerification(@RequestBody User user) {
        String email = user.getEmail();

        if (!CommonUtils.isEmail(email)) {
            throw new HustOjException(ExceptionCodeEnum.EMAIL_FORMAT_ERROR);
        }

        if (verificationCodeServiceImpl.isCodeExisted(email)) {
            throw new HustOjException(ExceptionCodeEnum.VERIFICATION_CODE_EXISTED);
        }

        mailService.sendSimpleMail(email, "HustOj验证码", "您的验证码为：" + verificationCodeServiceImpl.getOrGenerateCodeForUser(email));

        return Result.success("验证码发送成功");
    }

    /**
     * 注册
     */
    @PostMapping("/register/{code}")
    public Result register(@RequestBody User user, @PathVariable String code) {
        if (!verificationCodeServiceImpl.validateCode(user.getEmail(), code)) {
            throw new HustOjException(ExceptionCodeEnum.VERIFICATION_CODE_ERROR);
        }

        userService.register(user);
        return Result.success("注册成功");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        Map<String, Object> map = userService.login(user);
        return Result.success("登录成功", map);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result uploadAvatar(MultipartFile file) {
        String url = userService.uploadAvatar(file);
        return Result.success("上传头像成功", url);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result getUserInfo() {
        User user = userService.getUserInfo();
        return Result.success("获取用户信息成功", user);
    }
}
