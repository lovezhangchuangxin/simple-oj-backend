package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.hust.constant.FileType;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.mapper.UserMapper;
import edu.hust.pojo.User;
import edu.hust.service.MailService;
import edu.hust.service.UserService;
import edu.hust.utils.AliOSSUtils;
import edu.hust.utils.CommonUtils;
import edu.hust.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AliOSSUtils aliOSSUtils;
    private final MailService mailService;

    public UserServiceImpl(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder, AliOSSUtils aliOSSUtils, MailService mailService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.aliOSSUtils = aliOSSUtils;
        this.mailService = mailService;
    }

    /**
     * 注册
     */
    @Override
    public void register(User user) {
        // 非空判断
        if (!CommonUtils.isUsername(user.getUsername()) || !CommonUtils.isPassword(user.getPassword()) || !CommonUtils.isEmail(user.getEmail())) {
            throw new HustOjException(ExceptionCodeEnum.PARAMS_NULL);
        }

        // 查询数据库中是否有该用户
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername())) != null) {
            throw new HustOjException(ExceptionCodeEnum.USERNAME_EXISTED);
        }

        // 查询数据库中是否有该邮箱
        if (userMapper.selectOne(new QueryWrapper<User>().eq("email", user.getEmail())) != null) {
            throw new HustOjException(ExceptionCodeEnum.EMAIL_EXISTED);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 防止用户自行插入数据
        user.setId(null);
        user.setRole(0);
        user.setCreateTime(null);
        user.setUpdateTime(null);

        // 插入数据库，只插入用户名，密码，邮箱
        userMapper.insert(user);
    }

    /**
     * 登录
     */
    @Override
    public Map<String, Object> login(User user) {
        // 可以邮箱登录和用户名登录
        // 非空判断
        if ((user.getUsername() == null && user.getEmail() == null) || user.getPassword() == null) {
            throw new HustOjException(ExceptionCodeEnum.PARAMS_NULL);
        }

        // 查询数据库中是否有该用户
        User userInDB;
        if (user.getUsername() != null) {
            userInDB = userMapper.selectOne(new QueryWrapper<User>().eq("username", user.getUsername()));
        } else {
            userInDB = userMapper.selectOne(new QueryWrapper<User>().eq("email", user.getEmail()));
        }

        // 用户以及密码判断
        if (userInDB == null || !passwordEncoder.matches(user.getPassword(), userInDB.getPassword())) {
            throw new HustOjException(ExceptionCodeEnum.USER_NOT_FOUND, "用户名或密码错误");
        }

        // 生成 token
        String token = JwtUtils.genToken(Map.of("userId", userInDB.getId().toString()), userInDB.getRole() == 1);
        return Map.of("token", token, "user", filterUserSecretInfo(userInDB));
    }

    /**
     * 上传头像
     */
    @Override
    public String uploadAvatar(MultipartFile file) {
        Integer userId = JwtUtils.getUserId();
        String url = "";
        try {
            url = aliOSSUtils.upload(file, FileType.IMAGE);
        } catch (Exception e) {
            log.error("上传头像失败", e);
            throw new HustOjException(ExceptionCodeEnum.UPLOAD_AVATAR_ERROR);
        }

        User user = userMapper.selectById(userId);
        user.setAvatar(url);
        userMapper.updateById(user);
        return url;
    }

    /**
     * 获取用户信息
     */
    @Override
    public User getUserInfo() {
        // 拿到请求头中的 userId 属性
        Integer userId = JwtUtils.getUserId();
        User user = userMapper.selectById(userId);
        return filterUserSecretInfo(user);
    }

    /**
     * 过滤用户敏感信息
     */
    public User filterUserSecretInfo(User user) {
        if (user == null) {
            throw new HustOjException(ExceptionCodeEnum.USER_NOT_FOUND);
        }

        user.setPassword(null);
        return user;
    }

    /**
     * 修改用户信息
     */
    @Override
    public void updateUserInfo(User user) {
        Integer userId = JwtUtils.getUserId();
        User userInDB = userMapper.selectById(userId);

        if (userInDB == null) {
            throw new HustOjException(ExceptionCodeEnum.USER_NOT_FOUND);
        }

        boolean isAdmin = userInDB.getRole() == 1;

        // 自己只能改自己的，或者管理员可以改所有人
        if (!isAdmin && !Objects.equals(user.getId(), userInDB.getId())) {
            throw new HustOjException(ExceptionCodeEnum.PERMISSION_DENIED);
        }

        // 格式校验
        if ((user.getUsername() != null && !CommonUtils.isUsername(user.getUsername())) ||
                (user.getPassword() != null && !CommonUtils.isPassword(user.getPassword())) ||
                (user.getEmail() != null && !CommonUtils.isEmail(user.getEmail()))) {
            throw new HustOjException(ExceptionCodeEnum.PARAMS_NULL);
        }

        // 查询数据库中是否有该邮箱
        if (user.getEmail() != null && userMapper.selectOne(new QueryWrapper<User>().eq("email", user.getEmail())) != null) {
            throw new HustOjException(ExceptionCodeEnum.EMAIL_EXISTED);
        }

        // 只有管理员可以修改角色
        if (!isAdmin) {
            user.setRole(null);
        }

        // 密码加密
        if (user.getPassword() != null) {
            if (isAdmin) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                user.setPassword(null);
            }
        }

        user.setCreateTime(null);
        user.setUpdateTime(null);

        userMapper.updateById(user);
    }

    /**
     * 修改密码
     */
    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        Integer userId = JwtUtils.getUserId();
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new HustOjException(ExceptionCodeEnum.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new HustOjException(ExceptionCodeEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /**
     * 刷新 token
     */
    @Override
    public String refreshToken() {
        Integer userId = JwtUtils.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new HustOjException(ExceptionCodeEnum.USER_NOT_FOUND);
        }

        return JwtUtils.genToken(Map.of("userId", user.getId().toString()), user.getRole() == 1);
    }

    /**
     * 重置密码，忘记密码时设置随机密码并发送邮件告知
     */
    @Override
    public void resetPassword(String email) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
        if (user == null) {
            throw new HustOjException(ExceptionCodeEnum.USER_NOT_FOUND);
        }

        String newPassword = CommonUtils.genRandomPassword();

        // 发送邮件
        mailService.sendSimpleMail(email, "SimpleOj重置密码", "您的新密码为：" + newPassword + "，请尽快登录修改密码");

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
