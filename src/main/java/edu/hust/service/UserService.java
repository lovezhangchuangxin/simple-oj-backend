package edu.hust.service;

import edu.hust.pojo.User;

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
    String login(User user);
}
