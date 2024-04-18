package edu.hust.service;

import edu.hust.pojo.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {
    /**
     * 注册
     *
     * @param user 学生
     */
    void register(User user);

    /**
     * 登录
     *
     * @param user 学生
     */
    Map<String, Object> login(User user);


    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 头像地址
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    User getUserInfo();

    void updateUserInfo(User user);

    void updatePassword(String oldPassword, String newPassword);

    String refreshToken();

    void resetPassword(String email);
}
