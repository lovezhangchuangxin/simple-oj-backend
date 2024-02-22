package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.mapper.UserMapper;
import edu.hust.pojo.User;
import edu.hust.service.UserService;
import edu.hust.utils.CommonUtils;
import edu.hust.utils.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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
        user.setCreateTime(null);
        user.setUpdateTime(null);

        // 插入数据库，只插入用户名，密码，邮箱
        userMapper.insert(user);
    }

    /**
     * 登录
     */
    @Override
    public String login(User user) {
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

        // 生成token
        Map<String, String> map = Map.of("userId", userInDB.getId().toString());
        return JwtUtils.genToken(map);
    }

}
